package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.article.Replacement;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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

    // Save articles in batches to improve performance
    private DumpArticleCache cache = new DumpArticleCache();

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @TestOnly
    boolean processArticle(WikipediaPage dumpArticle) {
        return processArticle(dumpArticle, false);
    }

    boolean processArticle(WikipediaPage dumpArticle, boolean forceProcess) {
        LOGGER.debug("Processing article: {}...", dumpArticle.getTitle());

        if (!isDumpArticleProcessable(dumpArticle)) {
            LOGGER.debug("Article not processable by namespace or content");
            return false;
        }

        Collection<Replacement> dbReplacements = cache.findReplacementsByArticleId(dumpArticle.getId());

        LocalDate dbLastUpdate = dbReplacements.stream()
                .map(Replacement::getLastUpdate)
                .max(Comparator.comparing(LocalDate::toEpochDay)).orElse(LocalDate.now());

        if (!dbReplacements.isEmpty()
                && !isArticleProcessableByTimestamp(dumpArticle.getTimestamp(), dbLastUpdate, forceProcess)) {
            LOGGER.debug("Article not processable by date. Dump date: {} -  DB date: {}",
                    dumpArticle.getTimestamp(), dbLastUpdate);
            return false;
        }

        // Find replacements
        List<ArticleReplacement> articleReplacements = replacementFinderService.findReplacements(dumpArticle.getContent());

        if (articleReplacements.isEmpty()) {
            LOGGER.debug("No replacements found");
            articleService.deleteNotReviewedReplacements(dumpArticle.getId());
        } else {
            LOGGER.debug("Indexing found replacements...");
            cache.indexArticleReplacements(dumpArticle, articleReplacements);
        }

        return true;
    }

    private boolean isDumpArticleProcessable(WikipediaPage dumpArticle) {
        return PROCESSABLE_NAMESPACES.contains(dumpArticle.getNamespace()) && !dumpArticle.isRedirectionPage();
    }

    private boolean isArticleProcessableByTimestamp(LocalDate dumpDate, LocalDate dbDate, boolean forceProcess) {
        if (dumpDate.isAfter(dbDate)) {
            // Article modified in dump after last indexing. Reprocess always.
            return true;
        } else {
            // Article not modified in dump after last indexing. Reprocess when forcing.
            return forceProcess;
        }
    }

    void finish() {
        // The remaining cached articles are not in the dump so we remove them from DB
        cache.cleanCache();
    }

    private class DumpArticleCache {

        private static final int CACHE_SIZE = 1000;
        private Map<Integer, Collection<Replacement>> replacementMap;
        private int maxCachedId = 0;

        private Collection<Replacement> toIndex = new HashSet<>(CACHE_SIZE * 10);

        Collection<Replacement> findReplacementsByArticleId(int id) {
            if (replacementMap == null) {
                loadCache(id);
            }

            Collection<Replacement> replacements = replacementMap.get(id);
            replacementMap.remove(id); // No need to check if the ID exists

            if (id >= maxCachedId) {
                cleanCache();
            }

            return replacements == null ? Collections.emptySet() : replacements;
        }

        void indexArticleReplacements(WikipediaPage dumpArticle, Collection<ArticleReplacement> articleReplacements) {
            toIndex.addAll(articleService.convertArticleReplacements(dumpArticle, articleReplacements));
        }

        private void loadCache(int id) {
            // Load the cache of database articles
            // The first time we load the cache with the first 1000 articles
            int minId = replacementMap == null ? 0 : id - 1;
            List<Replacement> replacements = articleService.findAllReplacementsWithArticleIdGreaterThan(minId, CACHE_SIZE);
            replacementMap = articleService.buildReplacementMapByArticle(replacements);
            maxCachedId = replacementMap.keySet().stream().max(Comparator.comparingInt(Integer::valueOf)).orElse(-1);
        }

        private void cleanCache() {
            // Clear the cache if obsolete (we assume the dump articles are in order)
            // The remaining cached articles are not in the dump so we remove them from DB
            articleService.deleteArticles(replacementMap.keySet());
            replacementMap = new HashMap<>(CACHE_SIZE);

            // Send all replacements to index
            articleService.indexReplacements(toIndex);
        }

    }

}
