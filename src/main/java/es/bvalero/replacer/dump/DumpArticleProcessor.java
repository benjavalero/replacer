package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.persistence.Article;
import es.bvalero.replacer.persistence.ArticleRepository;
import es.bvalero.replacer.persistence.Replacement;
import es.bvalero.replacer.persistence.ReplacementRepository;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Process an article found in a Wikipedia dump.
 */
@Component
class DumpArticleProcessor {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(DumpArticleProcessor.class);
    private static final Collection<WikipediaNamespace> PROCESSABLE_NAMESPACES =
            EnumSet.of(WikipediaNamespace.ARTICLE, WikipediaNamespace.ANNEX);
    private static final int CACHE_SIZE = 1000;

    // Save articles in batches to improve performance
    private DumpArticleCache cache = new DumpArticleCache(CACHE_SIZE);
    private Collection<Article> articlesToDelete = new ArrayList<>(CACHE_SIZE);
    private Collection<Article> articlesToSave = new ArrayList<>(CACHE_SIZE);
    private Collection<Replacement> replacementsToDelete = new ArrayList<>(CACHE_SIZE);
    private Collection<Replacement> replacementsToAdd = new HashSet<>(CACHE_SIZE); // Set to avoid duplicates

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private ArticleService articleService;

    @TestOnly
    boolean processArticle(DumpArticle dumpArticle) {
        return processArticle(dumpArticle, false);
    }

    /**
     * Process a dump article: find the replacements and add them to the database.
     */
    boolean processArticle(DumpArticle dumpArticle, boolean forceProcess) {
        LOGGER.debug("Processing article: {}...", dumpArticle.getTitle());

        // Check namespace and redirection articles
        if (!checkArticleByNamespaceAndContent(dumpArticle)) {
            return false;
        }

        Article dbArticle = cache.findArticleById(dumpArticle.getId());

        // Check if reviewed after dump article timestamp
        if (dbArticle != null && !forceProcess && !checkArticleByDate(dbArticle, dumpArticle.getTimestamp())) {
            return false;
        }

        // Find replacements
        List<ArticleReplacement> articleReplacements = articleService.findReplacements(dumpArticle.getContent());

        if (articleReplacements.isEmpty()) {
            LOGGER.debug("No errors found in article: {}", dumpArticle.getTitle());
            if (dbArticle != null) {
                articlesToDelete.add(dbArticle);
            }
        } else if (dbArticle != null) {
            // Compare the new replacements with the existing ones
            List<Replacement> oldReplacements = replacementRepository.findByArticle(dbArticle);

            Collection<Replacement> newReplacements = new HashSet<>(articleReplacements.size());
            for (ArticleReplacement replacement : articleReplacements) {
                newReplacements.add(adaptArticleReplacement(replacement, dbArticle));
            }

            if (compareReplacements(oldReplacements, newReplacements)) {
                articlesToSave.add(dbArticle
                        .withTitle(dumpArticle.getTitle()) // In case the title of the article has changed
                        .withAdditionDate(LocalDateTime.now())
                        .withReviewDate(null));
            }
        } else {
            // Insert new article and its replacements in DB
            Article newArticle = Article.builder()
                    .setId(dumpArticle.getId())
                    .setTitle(dumpArticle.getTitle())
                    .build();
            articlesToSave.add(newArticle);

            // Add replacements in DB
            for (ArticleReplacement replacement : articleReplacements) {
                replacementsToAdd.add(adaptArticleReplacement(replacement, newArticle));
            }
        }

        return true;
    }

    private boolean checkArticleByNamespaceAndContent(DumpArticle dumpArticle) {
        // Check namespace and redirection articles
        return PROCESSABLE_NAMESPACES.contains(dumpArticle.getNamespace())
                && !WikipediaUtils.isRedirectionArticle(dumpArticle.getContent());
    }

    private boolean checkArticleByDate(Article dbArticle, LocalDateTime dumpTimestamp) {
        // We compare with seconds because the DB timestamps is milliseconds-level but the Dump date is not
        // and it is possible that the review date and the timestamp is exactly the same
        if (dbArticle.getReviewDate() != null &&
                !dbArticle.getReviewDate().truncatedTo(ChronoUnit.SECONDS)
                        .isBefore(dumpTimestamp.truncatedTo(ChronoUnit.SECONDS))) {
            LOGGER.debug("Article reviewed after dump timestamp. Skipping.");
            return false;
        }

        // Check if added after dump article timestamp
        if (dbArticle.getAdditionDate().truncatedTo(ChronoUnit.SECONDS)
                .isAfter(dumpTimestamp.truncatedTo(ChronoUnit.SECONDS))) {
            LOGGER.debug("Article added after dump timestamp. Skipping.");
            return false;
        }

        return true;
    }

    private Replacement adaptArticleReplacement(ArticleReplacement articleReplacement, Article article) {
        return Replacement.builder()
                .setArticle(article)
                .setType(articleReplacement.getType())
                .setText(articleReplacement.getSubtype())
                .build();
    }

    private boolean compareReplacements(Collection<Replacement> oldReplacements, Collection<Replacement> newReplacements) {
        boolean modified = false;

        for (Replacement oldReplacement : oldReplacements) {
            if (!newReplacements.contains(oldReplacement)) {
                replacementsToDelete.add(oldReplacement);
                modified = true;
            }
        }

        // Replacements to add to DB
        for (Replacement newReplacement : newReplacements) {
            if (!oldReplacements.contains(newReplacement)) {
                replacementsToAdd.add(newReplacement);
                modified = true;
            }
        }

        return modified;
    }

    void finish() {
        // The remaining cached articles are not in the dump so we remove them from DB
        cache.cleanCache();
    }

    private class DumpArticleCache {

        private Map<Integer, Article> articles;
        private int maxCachedId;

        DumpArticleCache(int size) {
            articles = new HashMap<>(size);
            maxCachedId = 0;
        }

        @Nullable
        Article findArticleById(int id) {
            if (articles.isEmpty()) {
                loadCache(id);
            }

            Article article = articles.get(id);
            if (article != null) {
                articles.remove(id);
            }

            if (id >= maxCachedId) {
                cleanCache();
            }

            return article;
        }

        private void loadCache(int id) {
            // Load the cache of database articles
            // The first time we load the cache with the first 1000 articles
            int minId = articles == null ? 0 : id - 1;
            if (articles == null) {
                articles = new HashMap<>(CACHE_SIZE);
            }
            for (Article article : articleRepository.findByIdGreaterThanOrderById(minId, PageRequest.of(0, CACHE_SIZE))) {
                articles.put(article.getId(), article);
                maxCachedId = article.getId();
            }
        }

        private void cleanCache() {
            // Clear the cache if obsolete (we assume the dump articles are in order)
            // The remaining cached articles are not in the dump so we remove them from DB
            articlesToDelete.addAll(articles.values());
            articles.clear();

            flushModifications();
        }

        private void flushModifications() {
            // There is a FK Replacement-Article, we need to remove the replacements first.
            if (!replacementsToDelete.isEmpty()) {
                replacementRepository.deleteInBatch(replacementsToDelete);
                replacementsToDelete.clear();
            }

            if (!articlesToDelete.isEmpty()) {
                for (Article articleToDelete : articlesToDelete) {
                    articleService.deleteArticle(articleToDelete);
                }
                articlesToDelete.clear();
            }

            if (!articlesToSave.isEmpty()) {
                articleRepository.saveAll(articlesToSave);
                articlesToSave.clear();
            }

            if (!replacementsToAdd.isEmpty()) {
                replacementRepository.saveAll(replacementsToAdd);
                replacementsToAdd.clear();
            }

            // Flush and clear to avoid memory leaks (we are performing millions of updates)
            articleRepository.flush();
            replacementRepository.flush();
            articleRepository.clear(); // This clears all the EntityManager
        }

    }

}
