package es.bvalero.replacer.page.index;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.save.IndexedPageStatus;
import es.bvalero.replacer.replacement.IndexedReplacement;
import es.bvalero.replacer.replacement.save.IndexedReplacementStatus;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Helper class to compare an indexable and an indexed page and return a set of changes to align them.
 * It could be a Utility class, but we implement it as a Component to mock it more easily.
 */
@Slf4j
@Component
class PageComparator {

    IndexedPage indexPageReplacements(
        IndexablePage page,
        Collection<Replacement> pageReplacements,
        @Nullable IndexedPage dbPage
    ) {
        // Precondition: the page to index cannot be previous to the indexed one
        if (dbPage != null && page.getLastUpdate().toLocalDate().isBefore(dbPage.getLastUpdate())) {
            LOGGER.warn(
                "Page to index previous to the indexed one: {} - {}",
                page.getLastUpdate(),
                dbPage.getLastUpdate()
            );
        }

        // Check changes in the page
        IndexedPageStatus pageStatus;
        if (dbPage == null) {
            // New page
            pageStatus = IndexedPageStatus.ADD;
        } else if (isUpdatePage(page, dbPage)) {
            // Update page if needed
            pageStatus = IndexedPageStatus.UPDATE;
        } else {
            pageStatus = IndexedPageStatus.INDEXED;
        }

        IndexedPage indexedPage = toIndexedPage(page, pageStatus);

        // Wrap the indexed replacements to compare them
        final List<ComparableReplacement> comparableDbReplacements = dbPage == null
            ? new LinkedList<>() // The collection must be mutable
            : dbPage
                .getReplacements()
                .stream()
                .map(ComparableReplacement::of)
                .collect(Collectors.toCollection(LinkedList::new));
        // Remove possible duplicates in database
        cleanDuplicatedReplacements(comparableDbReplacements).forEach(
            cr -> indexedPage.addReplacement(cr.toDomain(IndexedReplacementStatus.REMOVE))
        );

        // We compare each replacement found in the page to index with the ones existing in database
        // We add to the result the needed modifications to align the database with the actual replacements
        // Meanwhile we also modify the set of DB replacements to eventually contain the final result
        // All replacements modified in the DB set are marked as "touched".
        // In case a replacement is kept as is, we also mark it as "touched".
        final List<ComparableReplacement> comparablePageReplacements = pageReplacements
            .stream()
            .map(ComparableReplacement::of)
            .collect(Collectors.toCollection(LinkedList::new));
        // Replacements with the same type and position are considered equal
        // If they have different position but same context, and they are close enough,
        // they are also considered equal and only one will be indexed and returned to the user to be reviewed.
        cleanDuplicatedReplacements(comparablePageReplacements);

        for (ComparableReplacement comparablePageReplacement : comparablePageReplacements) {
            handleReplacement(comparablePageReplacement, comparableDbReplacements, indexedPage);
        }

        cleanUpDbReplacements(comparableDbReplacements).forEach(
            cr -> indexedPage.addReplacement(cr.toDomain(IndexedReplacementStatus.REMOVE))
        );

        assert indexedPage.getStatus() != IndexedPageStatus.UNDEFINED;
        assert indexedPage
            .getReplacements()
            .stream()
            .noneMatch(p -> p.getStatus() == IndexedReplacementStatus.UNDEFINED);

        return indexedPage;
    }

    @VisibleForTesting
    static IndexedPage toIndexedPage(IndexablePage indexablePage, IndexedPageStatus pageStatus) {
        return IndexedPage.builder()
            .pageKey(indexablePage.getPageKey())
            .title(indexablePage.getTitle())
            .lastUpdate(indexablePage.getLastUpdate().toLocalDate())
            .status(pageStatus)
            .build();
    }

    /** Removes the duplicates in the given collection and returns the removed ones */
    private Collection<ComparableReplacement> cleanDuplicatedReplacements(
        Collection<ComparableReplacement> replacements
    ) {
        final Set<ComparableReplacement> duplicated = new HashSet<>();
        for (ComparableReplacement r1 : replacements) {
            if (duplicated.contains(r1)) {
                continue;
            }
            for (ComparableReplacement r2 : replacements) {
                if (!r1.equals(r2) && r1.isSame(r2)) {
                    // Prefer to remove the not-reviewed
                    // If not remove the second one
                    duplicated.add(r1.isToBeReviewed() ? r1 : r2);
                }
            }
        }

        duplicated.forEach(replacements::remove);
        return duplicated;
    }

    /*
     * Check the given replacement with the ones in DB. Update the given DB list if needed.
     * Return true if the given page replacement is to be reviewed after the indexation.
     */
    private void handleReplacement(
        ComparableReplacement pageReplacement,
        Collection<ComparableReplacement> dbReplacements,
        IndexedPage indexedPage
    ) {
        final Optional<ComparableReplacement> existingDbReplacement = findSameReplacementInCollection(
            pageReplacement,
            dbReplacements
        );
        if (existingDbReplacement.isPresent()) {
            final ComparableReplacement dbReplacement = existingDbReplacement.get();
            final boolean handleReplacement = handleExistingReplacement(pageReplacement, dbReplacement);
            dbReplacements.remove(dbReplacement);
            // Merge the page replacement into the DB list even if it is not eventually persisted
            final ComparableReplacement updatedReplacement = dbReplacement
                .withStart(pageReplacement.getStart())
                .withContext(pageReplacement.getContext())
                .withTouched(true);
            dbReplacements.add(updatedReplacement);
            if (handleReplacement) {
                indexedPage.addReplacement(pageReplacement.toDomain(IndexedReplacementStatus.UPDATE));
            } else if (dbReplacement.isToBeReviewed()) {
                indexedPage.addReplacement(pageReplacement.toDomain(IndexedReplacementStatus.INDEXED));
            }
        } else {
            // New replacement
            dbReplacements.add(pageReplacement.withTouched(true));
            indexedPage.addReplacement(pageReplacement.toDomain(IndexedReplacementStatus.ADD));
        }
    }

    private Optional<ComparableReplacement> findSameReplacementInCollection(
        ComparableReplacement replacement,
        Collection<ComparableReplacement> entities
    ) {
        return entities.stream().filter(entity -> entity.isSame(replacement)).findAny();
    }

    /* Compare the given replacement with the counterpart in DB and return if it must be updated */
    private boolean handleExistingReplacement(ComparableReplacement replacement, ComparableReplacement dbReplacement) {
        // At this point we assume both replacements are the "same"
        return (
            dbReplacement.isToBeReviewed() &&
            (!Objects.equals(replacement.getStart(), dbReplacement.getStart()) ||
                !Objects.equals(replacement.getContext(), dbReplacement.getContext()))
        );
    }

    /* Check if it is needed to update the page in database */
    private boolean isUpdatePage(IndexablePage page, IndexedPage dbPage) {
        if (!Objects.equals(page.getTitle(), dbPage.getTitle())) {
            // Just in case check the title as it might change with time
            return true;
        } else {
            return dbPage.getLastUpdate().isBefore(page.getLastUpdate().toLocalDate());
        }
    }

    /* Removes obsolete replacements from the given collection and returns the removed ones */
    private Collection<ComparableReplacement> cleanUpDbReplacements(Collection<ComparableReplacement> dbReplacements) {
        // All remaining replacements to review (or system-reviewed)
        // and not checked so far are obsolete and thus to be deleted
        final List<ComparableReplacement> obsolete = dbReplacements
            .stream()
            .filter(ComparableReplacement::isObsolete)
            .toList();
        obsolete.forEach(dbReplacements::remove);
        return obsolete;
    }

    static Collection<Replacement> filterReplacementsToReview(
        Collection<Replacement> replacements,
        IndexedPage indexedPage
    ) {
        Collection<IndexedReplacement> indexedReplacements = indexedPage
            .getReplacements()
            .stream()
            .filter(IndexedReplacement::isToBeReviewed)
            .toList();
        return replacements
            .stream()
            .filter(r -> indexedReplacements.stream().anyMatch(ir -> PageComparator.isSameReplacement(r, ir)))
            .toList();
    }

    private static boolean isSameReplacement(Replacement r, IndexedReplacement ir) {
        return (
            r.getType().equals(ir.getType()) && r.getStart() == ir.getStart() && r.getContext().equals(ir.getContext())
        );
    }
}
