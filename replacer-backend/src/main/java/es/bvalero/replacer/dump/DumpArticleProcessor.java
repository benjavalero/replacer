package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleIndexService;
import es.bvalero.replacer.article.ReplacementEntity;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Process an article found in a Wikipedia dump.
 */
@Slf4j
@Component
class DumpArticleProcessor {

    @Autowired
    private DumpArticleCache dumpArticleCache;

    @Autowired
    private ArticleIndexService articleIndexService;

    @Autowired
    private ReplacementFinderService replacementFinderService;

    boolean isDumpArticleProcessable(WikipediaPage dumpArticle) {
        if (!isDumpArticleProcessableByNamespace(dumpArticle)) {
            LOGGER.debug("END Process dump article. Not processable by namespace: {}", dumpArticle.getTitle());
            return false;
        }
        if (!dumpArticle.isProcessableByContent()) {
            LOGGER.debug("END Process dump article. Not processable by content: {}", dumpArticle.getTitle());
            return false;
        }

        return true;
    }

    boolean processArticle(WikipediaPage dumpArticle) {
        LOGGER.debug("START Process dump article: {} - {}", dumpArticle.getId(), dumpArticle.getTitle());

        Collection<ReplacementEntity> dbReplacements = dumpArticleCache.findDatabaseReplacements(dumpArticle.getId());
        Optional<LocalDate> dbLastUpdate = dbReplacements.stream().map(ReplacementEntity::getLastUpdate)
                .max(Comparator.comparing(LocalDate::toEpochDay));
        if (dbLastUpdate.isPresent()
                && !isArticleProcessableByTimestamp(dumpArticle.getLastUpdate().toLocalDate(), dbLastUpdate.get())) {
            LOGGER.debug("END Process dump article. Not processable by date: {}. Dump date: {}. DB date: {}",
                    dumpArticle.getTitle(), dumpArticle.getLastUpdate().toLocalDate(), dbLastUpdate);
            return false;
        }

        List<Replacement> replacements = replacementFinderService.findReplacements(dumpArticle.getContent());
        articleIndexService.indexArticleReplacements(dumpArticle, replacements, dbReplacements);

        LOGGER.debug("END Process dump article: {}", dumpArticle.getTitle());
        return true;
    }

    private boolean isDumpArticleProcessableByNamespace(WikipediaPage dumpArticle) {
        return WikipediaNamespace.getProcessableNamespaces().contains(dumpArticle.getNamespace());
    }

    private boolean isArticleProcessableByTimestamp(LocalDate dumpDate, LocalDate dbDate) {
        // If article modified in dump equals to the last indexing, reprocess always.
        // If article modified in dump after last indexing, reprocess always.
        // If article modified in dump before last indexing, do not reprocess even when forcing.
        return !dumpDate.isBefore(dbDate);
    }

}
