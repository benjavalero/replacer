package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.page.index.IndexablePage;
import es.bvalero.replacer.page.index.IndexablePageValidator;
import es.bvalero.replacer.page.index.IndexableReplacement;
import es.bvalero.replacer.page.index.PageIndexHelper;
import es.bvalero.replacer.replacement.ReplacementEntity;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
    private PageReplacementService pageReplacementService;

    @Autowired
    private PageIndexHelper pageIndexHelper;

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private IndexablePageValidator indexablePageValidator;

    @Nullable
    List<ReplacementEntity> process(IndexablePage dumpPage) throws ReplacerException {
        // 1. Check if it is processable by namespace
        // We "skip" the item by throwing an exception
        try {
            indexablePageValidator.validateProcessable(dumpPage);
        } catch (ReplacerException e) {
            // If the page is not processable then it should not exist in DB ==> remove all replacements of this page
            // There could be replacements reviewed by users that we want to keep for the sake of statistics
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

    private List<ReplacementEntity> notProcessPage(IndexablePage dumpPage) {
        List<ReplacementEntity> dbReplacements = pageReplacementService.findByPageId(
            dumpPage.getId(),
            dumpPage.getLang()
        );

        // Return the DB replacements not reviewed in order to delete them
        return dbReplacements
            .stream()
            .filter(r -> r.isToBeReviewed() || r.isSystemReviewed())
            .map(ReplacementEntity::setToDelete)
            .collect(Collectors.toList());
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    @VisibleForTesting
    List<ReplacementEntity> processPage(IndexablePage dumpPage) {
        List<ReplacementEntity> dbReplacements = pageReplacementService.findByPageId(
            dumpPage.getId(),
            dumpPage.getLang()
        );
        Optional<LocalDate> dbLastUpdate = dbReplacements
            .stream()
            .map(ReplacementEntity::getLastUpdate)
            .max(Comparator.comparing(LocalDate::toEpochDay));
        if (
            dbLastUpdate.isPresent() &&
            isNotProcessableByTimestamp(dumpPage, dbLastUpdate.get()) &&
            isNotProcessableByPageTitle(dumpPage, dbReplacements)
        ) {
            LOGGER.trace(
                "Page not processable by date: {}. Dump date: {}. DB date: {}",
                dumpPage.getTitle(),
                dumpPage.getLastUpdate(),
                dbLastUpdate
            );
            return Collections.emptyList();
        }

        List<Replacement> replacements = replacementFinderService.find(convert(dumpPage));
        List<ReplacementEntity> toUpdate = pageIndexHelper.findIndexPageReplacements(
            dumpPage,
            replacements.stream().map(r -> convert(r, dumpPage)).collect(Collectors.toList()),
            dbReplacements
        );
        LOGGER.trace("Replacements to update: {}", toUpdate.size());
        return toUpdate;
    }

    private boolean isNotProcessableByPageTitle(IndexablePage dumpPage, List<ReplacementEntity> dbReplacements) {
        // In case the page title has changed we force the page processing
        return dbReplacements
            .stream()
            .filter(r -> !r.isDummy())
            .map(ReplacementEntity::getTitle)
            .allMatch(t -> dumpPage.getTitle().equals(t));
    }

    private boolean isNotProcessableByTimestamp(IndexablePage dumpPage, LocalDate dbDate) {
        // If page modified in dump equals to the last indexing, reprocess always.
        // If page modified in dump after last indexing, reprocess always.
        // If page modified in dump before last indexing, do not reprocess.
        return dumpPage.getLastUpdate().isBefore(dbDate);
    }

    private FinderPage convert(IndexablePage page) {
        return FinderPage.of(page.getLang(), page.getContent(), page.getTitle());
    }

    private IndexableReplacement convert(Replacement replacement, IndexablePage page) {
        return IndexableReplacement
            .builder()
            .lang(page.getLang())
            .pageId(page.getId())
            .type(replacement.getType().getLabel())
            .subtype(replacement.getSubtype())
            .position(replacement.getStart())
            .context(replacement.getContext(page.getContent()))
            .lastUpdate(page.getLastUpdate())
            .title(page.getTitle())
            .build();
    }
}
