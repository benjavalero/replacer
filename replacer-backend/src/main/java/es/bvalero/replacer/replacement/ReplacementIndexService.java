package es.bvalero.replacer.replacement;

import com.jcabi.aspects.Loggable;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReplacementIndexService {

    @Autowired
    private ReplacementDao replacementDao;

    /**
     * Update the replacements of a page in DB with the found ones
     */
    public void indexPageReplacements(IndexablePage page, List<IndexableReplacement> replacements) {
        // We need the page ID because the replacement list to index might be empty
        List<ReplacementEntity> toSave = findIndexPageReplacements(page, replacements, findDbReplacements(page));
        saveIndexedReplacements(toSave);
    }

    private List<ReplacementEntity> findDbReplacements(IndexablePage page) {
        return replacementDao.findByPageId(page.getId(), page.getLang());
    }

    /**
     * Compare the given found replacements in a page with the given ones in the database.
     *
     * @return A list of replacements to be inserted, updated or deleted in database.
     */
    @Loggable(prepend = true, value = Loggable.TRACE)
    public List<ReplacementEntity> findIndexPageReplacements(
        IndexablePage page,
        List<IndexableReplacement> replacements,
        List<ReplacementEntity> dbReplacements
    ) {
        List<ReplacementEntity> result = new ArrayList<>(100);

        // Ignore context when comparing replacements in case there are cases with the same context
        boolean ignoreContext =
            (replacements.size() != replacements.stream().map(IndexableReplacement::getContext).distinct().count()) ||
            (dbReplacements.size() != dbReplacements.stream().map(ReplacementEntity::getContext).distinct().count());
        replacements.forEach(
            replacement -> handleReplacement(replacement, dbReplacements, ignoreContext).ifPresent(result::add)
        );

        result.addAll(cleanUpPageReplacements(page, dbReplacements));
        return result;
    }

    /**
     * Check the given replacement with the ones in DB. Update the given DB list if needed.
     *
     * @return The updated replacement to be managed in DB if needed, or the new one to be created.
     */
    private Optional<ReplacementEntity> handleReplacement(
        IndexableReplacement replacement,
        List<ReplacementEntity> dbPageReplacements,
        boolean ignoreContext
    ) {
        Optional<ReplacementEntity> result;
        Optional<ReplacementEntity> existing = findSameReplacementInCollection(
            replacement,
            dbPageReplacements,
            ignoreContext
        );
        if (existing.isPresent()) {
            ReplacementEntity withAction = handleExistingReplacement(replacement, existing.get());
            dbPageReplacements.remove(existing.get());
            dbPageReplacements.add(withAction);
            result = withAction.isToUpdate() ? Optional.of(withAction) : Optional.empty();
        } else {
            // New replacement
            ReplacementEntity newReplacement = convert(replacement);
            dbPageReplacements.add(newReplacement);
            result = Optional.of(newReplacement);
            LOGGER.trace("Replacement inserted in DB: {}", replacement);
        }
        return result;
    }

    private Optional<ReplacementEntity> findSameReplacementInCollection(
        IndexableReplacement replacement,
        Collection<ReplacementEntity> entities,
        boolean ignoreContext
    ) {
        return entities.stream().filter(entity -> isSameReplacement(replacement, entity, ignoreContext)).findAny();
    }

    private boolean isSameReplacement(
        IndexableReplacement replacement,
        ReplacementEntity entity,
        boolean ignoreContext
    ) {
        if (
            replacement.getPageId() == entity.getPageId() &&
            replacement.getType().equals(entity.getType()) &&
            replacement.getSubtype().equals(entity.getSubtype())
        ) {
            if (ignoreContext) {
                return replacement.getPosition() == entity.getPosition();
            } else {
                return (
                    replacement.getPosition() == entity.getPosition() ||
                    replacement.getContext().equals(entity.getContext())
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
    private ReplacementEntity handleExistingReplacement(
        IndexableReplacement replacement,
        ReplacementEntity dbReplacement
    ) {
        ReplacementEntity result = dbReplacement.setToKeep(); // Initially we mark it to be kept
        if (dbReplacement.isToBeReviewed() && dbReplacement.isOlderThan(replacement.getLastUpdate())) {
            // The replacement is the same but the date is outdated in database
            result = result.updateLastUpdate(replacement.getLastUpdate());

            // Also update other values just in case any of them has changed
            if (replacement.getPosition() != dbReplacement.getPosition()) {
                result = result.updatePosition(replacement.getPosition());
            }
            if (!replacement.getContext().equals(dbReplacement.getContext())) {
                result = result.updateContext(replacement.getContext());
            }
            if (!replacement.getTitle().equals(dbReplacement.getTitle())) {
                result = result.updateTitle(replacement.getTitle());
            }
        }
        return result;
    }

    public void saveIndexedReplacements(List<ReplacementEntity> replacements) {
        try {
            List<ReplacementEntity> toInsert = replacements
                .stream()
                .filter(ReplacementEntity::isToCreate)
                .collect(Collectors.toList());
            if (!toInsert.isEmpty()) {
                replacementDao.insert(toInsert);
            }

            List<ReplacementEntity> toUpdateDate = replacements
                .stream()
                .filter(ReplacementEntity::isToUpdateDate)
                .collect(Collectors.toList());
            if (!toUpdateDate.isEmpty()) {
                replacementDao.updateDate(toUpdateDate);
            }

            List<ReplacementEntity> toUpdate = replacements
                .stream()
                .filter(ReplacementEntity::isToUpdateContext)
                .collect(Collectors.toList());
            if (!toUpdate.isEmpty()) {
                replacementDao.update(toUpdate);
            }

            List<ReplacementEntity> toRemove = replacements
                .stream()
                .filter(ReplacementEntity::isToDelete)
                .collect(Collectors.toList());
            if (!toRemove.isEmpty()) {
                replacementDao.delete(toRemove);
            }
        } catch (Exception e) {
            LOGGER.error("Error saving replacements: {}", replacements, e);
        }
    }

    @VisibleForTesting
    ReplacementEntity convert(IndexableReplacement replacement) {
        return ReplacementEntity
            .builder()
            .lang(replacement.getLang().getCode())
            .pageId(replacement.getPageId())
            .type(replacement.getType())
            .subtype(replacement.getSubtype())
            .position(replacement.getPosition())
            .context(replacement.getContext())
            .lastUpdate(replacement.getLastUpdate())
            .title(replacement.getTitle())
            .build()
            .setToCreate();
    }

    /**
     * Find obsolete replacements and add a dummy one if needed.
     *
     * @return A list of replacements to be managed in DB.
     */
    private List<ReplacementEntity> cleanUpPageReplacements(
        IndexablePage page,
        List<ReplacementEntity> dbReplacements
    ) {
        List<ReplacementEntity> result = new ArrayList<>(100);

        // Find just in case the system-reviewed replacements and delete them
        List<ReplacementEntity> systemReviewed = dbReplacements
            .stream()
            .filter(rep -> rep.isSystemReviewed() && !rep.isDummy())
            .collect(Collectors.toList());
        dbReplacements.removeAll(systemReviewed);
        result.addAll(systemReviewed.stream().map(ReplacementEntity::setToDelete).collect(Collectors.toList()));

        // All remaining replacements to review and not checked so far are obsolete and thus to be deleted
        List<ReplacementEntity> obsolete = dbReplacements
            .stream()
            .filter(rep -> rep.isToBeReviewed() && StringUtils.isEmpty(rep.getCudAction()))
            .collect(Collectors.toList());
        dbReplacements.removeAll(obsolete);
        result.addAll(obsolete.stream().map(ReplacementEntity::setToDelete).collect(Collectors.toList()));

        // Just in case check the title as it might change with time
        // The title could be null in case of indexing an obsolete dummy page
        if (StringUtils.isNotBlank(page.getTitle())) {
            List<ReplacementEntity> oldTitle = dbReplacements
                .stream()
                .filter(rep -> !rep.isDummy())
                .filter(rep -> !page.getTitle().equals(rep.getTitle()))
                .collect(Collectors.toList());
            result.addAll(oldTitle.stream().map(rep -> rep.updateTitle(page.getTitle())).collect(Collectors.toList()));
        }

        // We use a dummy replacement to store in some place the last update of the page
        // in case there are no replacements to review to store it instead.
        // The user-reviewed replacements can't be used as they are only kept for the sake of statistics
        // and have the date of the user review action.

        // Just in case check there is only one dummy
        List<ReplacementEntity> dummies = dbReplacements
            .stream()
            .filter(ReplacementEntity::isDummy)
            .sorted(Comparator.comparing(ReplacementEntity::getLastUpdate).reversed())
            .collect(Collectors.toList());
        if (dummies.size() > 1) {
            List<ReplacementEntity> obsoleteDummies = dummies.subList(1, dummies.size());
            dbReplacements.removeAll(obsoleteDummies);
            result.addAll(obsoleteDummies.stream().map(ReplacementEntity::setToDelete).collect(Collectors.toList()));
        }

        // If there remain replacements to review there is no need of dummy replacement
        // If not a dummy replacement must be created or updated (if older)
        // As this is the last step there is no need to update the DB list
        boolean existReplacementsToReview = dbReplacements.stream().anyMatch(ReplacementEntity::isToBeReviewed);
        Optional<ReplacementEntity> dummy = dummies.isEmpty() ? Optional.empty() : Optional.of(dummies.get(0));
        if (existReplacementsToReview) {
            dummy.ifPresent(d -> result.add(d.setToDelete()));
        } else {
            if (dummy.isPresent()) {
                if (dummy.get().isOlderThan(page.getLastUpdate())) {
                    result.add(dummy.get().updateLastUpdate(page.getLastUpdate()));
                }
            } else {
                result.add(ReplacementEntity.ofDummy(page.getId(), page.getLang(), page.getLastUpdate()));
            }
        }

        return result;
    }
}
