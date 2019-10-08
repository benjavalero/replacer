package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleIndexService;
import es.bvalero.replacer.article.ReplacementCache;
import es.bvalero.replacer.article.ReplacementEntity;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
    private ReplacementCache replacementCache;

    @Autowired
    private ArticleIndexService articleIndexService;

    @Autowired
    private ReplacementFinderService replacementFinderService;

    boolean processArticle(DumpArticle dumpArticle) {
        LOGGER.debug("START Process dump article: {} - {}", dumpArticle.getId(), dumpArticle.getTitle());

        List<ReplacementEntity> dbReplacements = replacementCache.findByArticleId(dumpArticle.getId());
        Optional<LocalDate> dbLastUpdate = dbReplacements.stream().map(ReplacementEntity::getLastUpdate)
                .max(Comparator.comparing(LocalDate::toEpochDay));
        if (dbLastUpdate.isPresent()
                && !dumpArticle.isProcessableByTimestamp(dbLastUpdate.get())) {
            LOGGER.debug("END Process dump article. Not processable by date: {}. Dump date: {}. DB date: {}",
                    dumpArticle.getTitle(), dumpArticle.getLastUpdate().toLocalDate(), dbLastUpdate);
            return false;
        }

        List<Replacement> replacements = replacementFinderService.findReplacements(dumpArticle.getContent());
        articleIndexService.indexArticleReplacements(dumpArticle, replacements, dbReplacements);

        LOGGER.debug("END Process dump article: {}", dumpArticle.getTitle());
        return true;
    }

    void finishOverallProcess() {
        this.replacementCache.clean();
    }

}
