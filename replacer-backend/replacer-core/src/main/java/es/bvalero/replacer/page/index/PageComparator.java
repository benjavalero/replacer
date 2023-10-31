package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.page.IndexedPage;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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

    PageComparatorResult indexPageReplacements(
        IndexablePage page,
        Collection<Replacement> pageReplacements,
        @Nullable IndexedPage dbPage
    ) {
        PageComparatorResult result = PageComparatorResult.of(page.getPageKey().getLang());

        // Precondition: the page to index cannot be previous to the indexed one
        if (dbPage != null && page.getLastUpdate().toLocalDate().isBefore(dbPage.getLastUpdate())) {
            String msg = String.format(
                "Page to index previous to the indexed one: %s - %s",
                page.getLastUpdate(),
                dbPage.getLastUpdate()
            );
            throw new IllegalArgumentException(msg);
        }

        // Check changes in the page
        if (dbPage == null) {
            // New page
            result.addPageToCreate(toIndexedPage(page));
        } else if (isUpdatePage(page, dbPage)) {
            // Update page if needed
            result.addPageToUpdate(toIndexedPage(page));
        }

        // Wrap the indexed replacements to compare them
        final List<ComparableReplacement> comparableDbReplacements = dbPage == null
            ? new LinkedList<>() // The collection must be mutable
            : dbPage
                .getReplacements()
                .stream()
                .map(ComparableReplacement::of)
                .collect(Collectors.toCollection(LinkedList::new));
        final Collection<ReplacementType> dbReplacementTypesToReview = comparableDbReplacements
            .stream()
            .filter(ComparableReplacement::isToBeReviewed)
            .map(ComparableReplacement::getType)
            .collect(Collectors.toUnmodifiableSet());
        // Remove possible duplicates in database
        cleanDuplicatedReplacements(comparableDbReplacements).forEach(result::addReplacementToDelete);

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
            handleReplacement(comparablePageReplacement, comparableDbReplacements, result);
        }

        cleanUpDbReplacements(comparableDbReplacements).forEach(result::addReplacementToDelete);

        // At this point the collection of DB replacements contains only reviewed items and the ones to review
        result.addReplacementsToReview(
            pageReplacements
                .stream()
                .filter(r ->
                    comparableDbReplacements
                        .stream()
                        .anyMatch(cr -> cr.isToBeReviewed() && cr.equals(ComparableReplacement.of(r)))
                )
                .toList()
        );

        // Calculate the new and obsolete replacement types for the page
        final Collection<ReplacementType> actualReplacementTypesToReview = result
            .getReplacementsToReview()
            .stream()
            .map(Replacement::getType)
            .collect(Collectors.toUnmodifiableSet());
        result.addReplacementTypesToCreate(
            CollectionUtils.removeAll(actualReplacementTypesToReview, dbReplacementTypesToReview)
        );
        result.addReplacementTypesToDelete(
            CollectionUtils.removeAll(dbReplacementTypesToReview, actualReplacementTypesToReview)
        );

        return result;
    }

    @VisibleForTesting
    static IndexedPage toIndexedPage(IndexablePage indexablePage) {
        return IndexedPage
            .builder()
            .pageKey(indexablePage.getPageKey())
            .title(indexablePage.getTitle())
            .lastUpdate(indexablePage.getLastUpdate().toLocalDate())
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
        PageComparatorResult result
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
                result.addReplacementToUpdate(updatedReplacement);
            }
        } else {
            // New replacement
            dbReplacements.add(pageReplacement.withTouched(true));
            result.addReplacementToCreate(pageReplacement);
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
}
