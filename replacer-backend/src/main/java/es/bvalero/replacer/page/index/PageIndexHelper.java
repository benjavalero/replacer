package es.bvalero.replacer.page.index;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Helper class to compare an indexable and an indexed page and return a set of changes to align them.
 *
 * It could be a Utility class but we implement it as a Component to mock it more easily.
 */
@Component
class PageIndexHelper {

    PageIndexResult indexPageReplacements(IndexablePage page, @Nullable IndexablePage dbPage) {
        PageIndexResult pageIndexResult = PageIndexResult.ofEmpty();
        Set<IndexableReplacement> dbReplacements = dbPage == null
            ? new HashSet<>() // The set must be mutable
            : new HashSet<>(dbPage.getReplacements());

        // Ignore context when comparing replacements in case there are cases with the same context
        boolean ignoreContext =
            existReplacementsWithSameContext(page.getReplacements()) ||
            existReplacementsWithSameContext(dbReplacements);
        for (IndexableReplacement replacement : page.getReplacements()) {
            pageIndexResult.add(handleReplacement(replacement, dbReplacements, ignoreContext));
        }

        // Check new page
        if (dbPage == null) {
            pageIndexResult.add(PageIndexResult.builder().createPages(Set.of(page)).build());
        } else {
            if (!Objects.equals(page.getTitle(), dbPage.getTitle())) {
                // Just in case check the title as it might change with time
                // The title could be null in case of indexing an obsolete dummy page

                pageIndexResult.add(PageIndexResult.builder().updatePages(Set.of(page)).build());
            }
        }

        pageIndexResult.add(cleanUpPageReplacements(page, dbReplacements));
        return pageIndexResult;
    }

    private boolean existReplacementsWithSameContext(Collection<IndexableReplacement> replacements) {
        return replacements.size() != replacements.stream().map(IndexableReplacement::getContext).distinct().count();
    }

    /**
     * Check the given replacement with the ones in DB. Update the given DB list if needed.
     *
     * @return The updated replacement to be managed in DB if needed, or the new one to be created.
     */
    private PageIndexResult handleReplacement(
        IndexableReplacement replacement,
        Set<IndexableReplacement> dbPageReplacements,
        boolean ignoreContext
    ) {
        PageIndexResult result;
        Optional<IndexableReplacement> existing = findSameReplacementInCollection(
            replacement,
            dbPageReplacements,
            ignoreContext
        );
        if (existing.isPresent()) {
            IndexableReplacement dbReplacement = existing.get();
            IndexableReplacement handledReplacement = handleExistingReplacement(replacement, dbReplacement);
            dbPageReplacements.remove(dbReplacement);
            if (handledReplacement.equals(dbReplacement)) {
                result = PageIndexResult.ofEmpty();
            } else {
                dbPageReplacements.add(handledReplacement.withTouched(true));
                result = PageIndexResult.builder().updateReplacements(Set.of(handledReplacement)).build();
            }
        } else {
            // New replacement
            dbPageReplacements.add(replacement.withTouched(true));
            result = PageIndexResult.builder().createReplacements(Set.of(replacement)).build();
        }
        return result;
    }

    private Optional<IndexableReplacement> findSameReplacementInCollection(
        IndexableReplacement replacement,
        Collection<IndexableReplacement> entities,
        boolean ignoreContext
    ) {
        return entities.stream().filter(entity -> isSameReplacement(replacement, entity, ignoreContext)).findAny();
    }

    private boolean isSameReplacement(
        IndexableReplacement replacement,
        IndexableReplacement entity,
        boolean ignoreContext
    ) {
        if (
            Objects.equals(replacement.getIndexablePageId(), entity.getIndexablePageId()) &&
            Objects.equals(replacement.getType(), entity.getType()) &&
            Objects.equals(replacement.getSubtype(), entity.getSubtype())
        ) {
            if (ignoreContext) {
                return Objects.equals(replacement.getPosition(), entity.getPosition());
            } else {
                return (
                    Objects.equals(replacement.getPosition(), entity.getPosition()) ||
                    Objects.equals(replacement.getContext(), entity.getContext())
                );
            }
        } else {
            return false;
        }
    }

    /**
     * Check the given replacement with the counterpart in DB.
     *
     * @return The original DB replacement update plus the action to do.
     */
    private IndexableReplacement handleExistingReplacement(
        IndexableReplacement replacement,
        IndexableReplacement dbReplacement
    ) {
        // Check if the replacement must be updated in DB
        if (
            dbReplacement.isToBeReviewed() &&
            (
                dbReplacement.isOlderThan(replacement) ||
                !Objects.equals(replacement.getPosition(), dbReplacement.getPosition()) ||
                !Objects.equals(replacement.getContext(), dbReplacement.getContext())
            )
        ) {
            return dbReplacement
                .withPosition(replacement.getPosition())
                .withContext(replacement.getContext())
                .withLastUpdate(replacement.getLastUpdate());
        } else {
            return dbReplacement;
        }
    }

    /**
     * Find obsolete replacements and add a dummy one if needed.
     *
     * @return A list of replacements to be managed in DB.
     */
    private PageIndexResult cleanUpPageReplacements(IndexablePage page, Set<IndexableReplacement> dbReplacements) {
        PageIndexResult result = PageIndexResult.ofEmpty();

        // Find just in case the system-reviewed replacements and delete them
        Set<IndexableReplacement> systemReviewed = dbReplacements
            .stream()
            .filter(rep -> rep.isSystemReviewed() && !rep.isDummy())
            .collect(Collectors.toSet());
        systemReviewed.forEach(dbReplacements::remove);
        result.add(PageIndexResult.builder().deleteReplacements(systemReviewed).build());

        // All remaining replacements to review and not checked so far are obsolete and thus to be deleted
        Set<IndexableReplacement> obsolete = dbReplacements
            .stream()
            .filter(rep -> rep.isToBeReviewed() && !rep.isTouched())
            .collect(Collectors.toSet());
        obsolete.forEach(dbReplacements::remove);
        result.add(PageIndexResult.builder().deleteReplacements(obsolete).build());

        // We use a dummy replacement to store in some place the last update of the page
        // in case there are no replacements to review to store it instead.
        // The user-reviewed replacements can't be used as they are only kept for the sake of statistics
        // and have the date of the user review action.

        // Just in case check there is only one dummy
        List<IndexableReplacement> dummies = dbReplacements
            .stream()
            .filter(IndexableReplacement::isDummy)
            .sorted(Comparator.comparing(IndexableReplacement::getLastUpdate).reversed())
            .collect(Collectors.toList());
        if (dummies.size() > 1) {
            Set<IndexableReplacement> obsoleteDummies = new HashSet<>(dummies.subList(1, dummies.size()));
            obsoleteDummies.forEach(dbReplacements::remove);
            result.add(PageIndexResult.builder().deleteReplacements(obsoleteDummies).build());
        }

        // If there remain replacements to review there is no need of dummy replacement
        // If not a dummy replacement must be created or updated (if older)
        // As this is the last step there is no need to update the DB list
        boolean existReplacementsToReview = dbReplacements.stream().anyMatch(IndexableReplacement::isToBeReviewed);
        Optional<IndexableReplacement> dummy = dummies.isEmpty() ? Optional.empty() : Optional.of(dummies.get(0));
        if (existReplacementsToReview) {
            dummy.ifPresent(
                indexableReplacement ->
                    result.add(PageIndexResult.builder().deleteReplacements(Set.of(indexableReplacement)).build())
            );
        } else {
            if (dummy.isPresent()) {
                if (
                    !page.getReplacements().isEmpty() &&
                    dummy.get().isOlderThan(Objects.requireNonNull(page.getLastUpdate()))
                ) {
                    result.add(
                        PageIndexResult
                            .builder()
                            .updateReplacements(
                                Set.of(dummy.get().withLastUpdate(Objects.requireNonNull(page.getLastUpdate())))
                            )
                            .build()
                    );
                }
            } else {
                result.add(
                    PageIndexResult.builder().createReplacements(Set.of(IndexableReplacement.ofDummy(page))).build()
                );
            }
        }

        return result;
    }
}
