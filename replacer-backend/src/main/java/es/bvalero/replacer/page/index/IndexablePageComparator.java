package es.bvalero.replacer.page.index;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Helper class to compare an indexable and an indexed page and return a set of changes to align them.
 *
 * It could be a Utility class, but we implement it as a Component to mock it more easily.
 */
@Component
class IndexablePageComparator {

    PageIndexResult indexPageReplacements(IndexablePage page, @Nullable IndexablePage dbPage) {
        PageIndexResult result = PageIndexResult.ofEmpty();

        // Use a Set for page replacements as we don't want to index duplicated replacements
        // Use a List for DB replacements as we want to detect possible duplicated in database
        final Set<IndexableReplacement> pageReplacements = new HashSet<>(page.getReplacements());
        final List<IndexableReplacement> dbReplacements = dbPage == null
            ? new ArrayList<>() // The collection must be mutable
            : new ArrayList<>(dbPage.getReplacements());

        // We compare each replacement found in the page to index with the ones existing in database
        // We add to the result the needed modifications to align the database with the actual replacements
        // Meanwhile we also modify the set of DB replacements to eventually contain the final result
        // All replacements modified in the DB set are marked as "touched".
        // In case a replacement is kept as is, we also mark it as "touched".
        // Replacements with the same position or context are considered equal and only one will be indexed
        for (IndexableReplacement replacement : pageReplacements) {
            result = result.add(handleReplacement(replacement, dbReplacements));
        }

        // Check changes in the page
        if (dbPage == null) {
            // New page
            result = result.add(PageIndexResult.builder().addPages(Set.of(page)).build());
        } else if (isUpdatePage(page, dbPage)) {
            // Update page if needed
            result = result.add(PageIndexResult.builder().updatePages(Set.of(page)).build());
        }

        return result.add(cleanUpPageReplacements(dbReplacements));
    }

    /* Check the given replacement with the ones in DB. Update the given DB list if needed. */
    private PageIndexResult handleReplacement(
        IndexableReplacement replacement,
        Collection<IndexableReplacement> dbPageReplacements
    ) {
        final PageIndexResult result;
        final Optional<IndexableReplacement> existing = findSameReplacementInCollection(
            replacement,
            dbPageReplacements
        );
        if (existing.isPresent()) {
            final IndexableReplacement dbReplacement = existing.get();
            final boolean handleReplacement = handleExistingReplacement(replacement, dbReplacement);
            dbPageReplacements.remove(dbReplacement);
            final IndexableReplacement updatedReplacement = replacement.withId(dbReplacement.getId()).withTouched(true);
            dbPageReplacements.add(updatedReplacement);
            if (handleReplacement) {
                result = PageIndexResult.builder().updateReplacements(Set.of(updatedReplacement)).build();
            } else {
                result = PageIndexResult.ofEmpty();
            }
        } else {
            // New replacement
            dbPageReplacements.add(replacement.withTouched(true));
            result = PageIndexResult.builder().addReplacements(Set.of(replacement)).build();
        }
        return result;
    }

    private Optional<IndexableReplacement> findSameReplacementInCollection(
        IndexableReplacement replacement,
        Collection<IndexableReplacement> entities
    ) {
        return entities.stream().filter(entity -> Objects.equals(replacement, entity)).findAny();
    }

    /* Compare the given replacement with the counterpart in DB and return if it must be updated */
    private boolean handleExistingReplacement(IndexableReplacement replacement, IndexableReplacement dbReplacement) {
        return (
            dbReplacement.isToBeReviewed() &&
            (
                !Objects.equals(replacement.getPosition(), dbReplacement.getPosition()) ||
                !Objects.equals(replacement.getContext(), dbReplacement.getContext())
            )
        );
    }

    /* Check if it is needed to update the page in database */
    private boolean isUpdatePage(IndexablePage page, IndexablePage dbPage) {
        if (!Objects.equals(page.getTitle(), dbPage.getTitle())) {
            // Just in case check the title as it might change with time
            return true;
        } else {
            return dbPage.getLastUpdate().isBefore(page.getLastUpdate());
        }
    }

    /**
     * Find obsolete replacements and add a dummy one if needed.
     *
     * @return A list of replacements to be managed in DB.
     */
    private PageIndexResult cleanUpPageReplacements(Collection<IndexableReplacement> dbReplacements) {
        PageIndexResult result = PageIndexResult.ofEmpty();

        // All remaining replacements to review (or system-reviewed)
        // and not checked so far are obsolete and thus to be deleted
        final Set<IndexableReplacement> obsolete = dbReplacements
            .stream()
            .filter(rep -> !rep.isTouched())
            .filter(rep -> rep.isToBeReviewed() || rep.isSystemReviewed())
            .collect(Collectors.toSet());
        obsolete.forEach(dbReplacements::remove);
        result = result.add(PageIndexResult.builder().removeReplacements(obsolete).build());

        return result;
    }
}
