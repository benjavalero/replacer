package es.bvalero.replacer.dump;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Process an article found in a Wikipedia dump.
 */
@Slf4j
@Component
class DumpArticleProcessor {

    @Autowired
    private ReplacementCache replacementCache;

    @Autowired
    private ReplacementIndexService replacementIndexService;

    @Autowired
    private ReplacementFindService replacementFindService;

    boolean processArticle(DumpArticle dumpArticle) {
        LOGGER.debug("START Process dump article: {} - {}", dumpArticle.getId(), dumpArticle.getTitle());

        List<ReplacementEntity> dbReplacements = replacementCache.findByArticleId(dumpArticle.getId());
        Optional<LocalDate> dbLastUpdate = dbReplacements.stream().map(ReplacementEntity::getLastUpdate)
                .max(Comparator.comparing(LocalDate::toEpochDay));
        if (dbLastUpdate.isPresent()
                && !dumpArticle.isProcessableByTimestamp(dbLastUpdate.get())) {
            LOGGER.debug("END Process dump article. Not processable by date: {}. Dump date: {}. DB date: {}",
                    dumpArticle.getTitle(), dumpArticle.getLastUpdate(), dbLastUpdate);
            return false;
        }

        List<Replacement> replacements = replacementFindService.findReplacements(dumpArticle.getContent());
        replacementIndexService.indexArticleReplacements(dumpArticle.getId(),
                replacements.stream().map(dumpArticle::convertReplacementToIndexed).collect(Collectors.toList()),
                dbReplacements);

        LOGGER.debug("END Process dump article: {}", dumpArticle.getTitle());
        return true;
    }

    void finishOverallProcess() {
        this.replacementCache.clean();
    }

}
