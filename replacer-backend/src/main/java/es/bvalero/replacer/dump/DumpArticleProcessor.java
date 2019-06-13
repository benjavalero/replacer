package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.article.Replacement;
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

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @TestOnly
    boolean processArticle(WikipediaPage dumpArticle) {
        return processArticle(dumpArticle, Collections.emptyList(), false);
    }

    @TestOnly
    boolean processArticle(WikipediaPage dumpArticle, Collection<Replacement> dbReplacements) {
        return processArticle(dumpArticle, dbReplacements, false);
    }

    boolean processArticle(WikipediaPage dumpArticle, Collection<Replacement> dbReplacements, boolean forceProcess) {
        LOGGER.debug("Process dump article: {} - {}", dumpArticle.getId(), dumpArticle.getTitle());

        if (!isDumpArticleProcessableByNamespace(dumpArticle)) {
            LOGGER.debug("Dump article not processable by namespace: {}", dumpArticle.getTitle());
            return false;
        }
        if (!isDumpArticleProcessableByContent(dumpArticle)) {
            LOGGER.debug("Dump article not processable by content: {}", dumpArticle.getTitle());
            return false;
        }

        Optional<LocalDate> dbLastUpdate = dbReplacements.stream().map(Replacement::getLastUpdate).max(Comparator.comparing(LocalDate::toEpochDay));
        if (dbLastUpdate.isPresent()
                && !isArticleProcessableByTimestamp(dumpArticle.getLastUpdate().toLocalDate(), dbLastUpdate.get(), forceProcess)) {
            LOGGER.debug("Dump article not processable by date: {}. Dump date: {}. DB date: {}",
                    dumpArticle.getTitle(), dumpArticle.getTimestamp(), dbLastUpdate);
            return false;
        }

        articleService.indexReplacements(
                articleService.convertArticleReplacements(dumpArticle, replacementFinderService.findReplacements(dumpArticle.getContent())),
                dbReplacements,
                true);

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

}
