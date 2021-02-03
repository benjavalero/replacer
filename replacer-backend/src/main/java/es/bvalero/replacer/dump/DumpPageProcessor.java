package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Process a page found in a Wikipedia dump.
 */
@Slf4j
@Component
class DumpPageProcessor {

    @Autowired
    private ReplacementCache replacementCache;

    @Autowired
    private ReplacementIndexService replacementIndexService;

    @Autowired
    private ReplacementFindService replacementFindService;

    @Resource
    private List<String> ignorableTemplates;

    @Nullable
    List<ReplacementEntity> process(DumpPage dumpPage) throws ReplacerException {
        // 1. Check if it is processable by content
        // We "skip" the item by throwing an exception
        try {
            dumpPage.validateProcessable(ignorableTemplates);
        } catch (ReplacerException e) {
            // Remove possible existing (not reviewed) replacements
            List<ReplacementEntity> toDelete = notProcessPage(dumpPage);
            if (toDelete.isEmpty()) {
                throw e;
            } else {
                return toDelete;
            }
        }

        // 2. Find the replacements to index
        try {
            List<ReplacementEntity> replacements = processPage(dumpPage);

            // We "filter" the item by returning NULL
            return replacements.isEmpty() ? null : replacements;
        } catch (Exception e) {
            // Just in case capture possible exceptions to continue processing other pages
            LOGGER.error("Page not processed: {}", dumpPage, e);
            throw new ReplacerException("Page not processable by exception", e);
        }
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    @VisibleForTesting
    List<ReplacementEntity> processPage(DumpPage dumpPage) {
        List<ReplacementEntity> dbReplacements = replacementCache.findByPageId(dumpPage.getId(), dumpPage.getLang());
        Optional<LocalDate> dbLastUpdate = dbReplacements
            .stream()
            .map(ReplacementEntity::getLastUpdate)
            .max(Comparator.comparing(LocalDate::toEpochDay));
        if (dbLastUpdate.isPresent() && !dumpPage.isProcessableByTimestamp(dbLastUpdate.get())) {
            LOGGER.trace(
                "Page not processable by date: {}. Dump date: {}. DB date: {}",
                dumpPage.getTitle(),
                dumpPage.getLastUpdate(),
                dbLastUpdate
            );
            return Collections.emptyList();
        }

        List<Replacement> replacements = replacementFindService.findReplacements(dumpPage);
        return replacementIndexService.findIndexPageReplacements(
            dumpPage,
            replacements.stream().map(dumpPage::convertReplacementToIndexed).collect(Collectors.toList()),
            dbReplacements
        );
    }

    private List<ReplacementEntity> notProcessPage(DumpPage dumpPage) {
        List<ReplacementEntity> dbReplacements = replacementCache.findByPageId(dumpPage.getId(), dumpPage.getLang());

        // Return the DB replacements not reviewed in order to delete them
        List<ReplacementEntity> toDelete = dbReplacements
            .stream()
            .filter(ReplacementEntity::isToBeReviewed)
            .filter(ReplacementEntity::isSystemReviewed)
            .collect(Collectors.toList());
        toDelete.forEach(ReplacementEntity::setToDelete);
        return toDelete;
    }
}
