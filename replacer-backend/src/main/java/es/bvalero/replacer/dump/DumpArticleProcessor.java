package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.article.ArticleTimestamp;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
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
        LOGGER.debug("Process dump article: {} - {}", dumpArticle.getId(), dumpArticle.getTitle());

        if (!isDumpArticleProcessableByNamespace(dumpArticle)) {
            LOGGER.debug("Dump article not processable by namespace: {}", dumpArticle.getTitle());
            return false;
        }
        if (!isDumpArticleProcessableByContent(dumpArticle)) {
            LOGGER.debug("Dump article not processable by content: {}", dumpArticle.getTitle());
            return false;
        }

        Optional<LocalDate> dbLastUpdate = cache.findArticleLastUpdate(dumpArticle.getId());
        if (dbLastUpdate.isPresent()
                && !isArticleProcessableByTimestamp(dumpArticle.getTimestamp(), dbLastUpdate.get(), forceProcess)) {
            LOGGER.debug("Dump article not processable by date: {}. Dump date: {}. DB date: {}",
                    dumpArticle.getTitle(), dumpArticle.getTimestamp(), dbLastUpdate);
            return false;
        }

        cache.indexArticleReplacementsInBatch(
                dumpArticle,
                replacementFinderService.findReplacements(dumpArticle.getContent()));

        return true;
    }

    private boolean isDumpArticleProcessableByNamespace(WikipediaPage dumpArticle) {
        return PROCESSABLE_NAMESPACES.contains(dumpArticle.getNamespace());
    }

    private boolean isDumpArticleProcessableByContent(WikipediaPage dumpArticle) {
        return !dumpArticle.isRedirectionPage();
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
        private Map<Integer, LocalDate> articleTimestamps = new HashMap<>(CACHE_SIZE);
        private int maxCachedId;
        private Map<WikipediaPage, Collection<ArticleReplacement>> articleReplacementMap = new HashMap<>(CACHE_SIZE);

        Optional<LocalDate> findArticleLastUpdate(int id) {
            // Load the cache the first time
            if (maxCachedId == 0) {
                loadCache(1);
            }

            LocalDate lastUpdate = articleTimestamps.get(id);
            articleTimestamps.remove(id); // No need to check if the ID exists

            if (id >= maxCachedId) {
                cleanCache();
                loadCache(Math.max(maxCachedId + 1, id));
            }

            return Optional.ofNullable(lastUpdate);
        }

        private void loadCache(int id) {
            LOGGER.debug("Load timestamps from database to cache. Min ID: {}", id);
            maxCachedId = id + CACHE_SIZE - 1;
            for (ArticleTimestamp timestamp : articleService.findMaxLastUpdateByArticleIdIn(id, maxCachedId)) {
                articleTimestamps.put(timestamp.getArticleId(), timestamp.getLastUpdate());
            }
        }

        private void cleanCache() {
            // Clear the cache if obsolete (we assume the dump articles are in order)
            // The remaining cached articles are not in the dump so we remove them from DB
            LOGGER.debug("Delete obsolete articles in DB: {}", articleTimestamps.size());
            articleService.deleteArticles(articleTimestamps.keySet());
            articleTimestamps = new HashMap<>(CACHE_SIZE);

            // Really send the article replacements to index in batch
            articleService.indexArticleReplacementsInBatch(articleReplacementMap);
            articleReplacementMap = new HashMap<>(CACHE_SIZE);
        }

        void indexArticleReplacementsInBatch(WikipediaPage page, Collection<ArticleReplacement> articleReplacements) {
            articleReplacementMap.put(page, articleReplacements);
        }

    }

}
