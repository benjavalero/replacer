package es.bvalero.replacer.replacement;

import es.bvalero.replacer.page.IndexablePage;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReplacementIndexService {
    @Autowired
    private ReplacementDao replacementDao;

    @Autowired
    private ModelMapper modelMapper;

    /** Update the replacements of a page in DB with the found ones */
    public void indexPageReplacements(IndexablePage page, List<IndexableReplacement> replacements) {
        // All replacements correspond to the same page
        if (replacements.stream().anyMatch(r -> r.getPageId() != page.getId())) {
            throw new IllegalArgumentException("Indexable replacements from more than one page");
        }

        // We need the page ID because the replacement list to index might be empty
        List<ReplacementEntity> toSave = findIndexPageReplacements(
            page,
            replacements,
            replacementDao.findByPageId(page.getId(), page.getLang())
        );
        saveReplacements(toSave);
    }

    /**
     * Compare the given found replacements in a page with the given ones in the database.
     *
     * @return A list of replacements to be inserted, updated or deleted in database.
     */
    public List<ReplacementEntity> findIndexPageReplacements(
        IndexablePage page,
        List<IndexableReplacement> replacements,
        List<ReplacementEntity> dbReplacements
    ) {
        LOGGER.debug(
            "START Index list of replacements. Page: {}\n" + "New: {} - {}\n" + "Old: {} - {}",
            page,
            replacements.size(),
            replacements,
            dbReplacements.size(),
            dbReplacements
        );
        List<ReplacementEntity> result = new ArrayList<>();

        // Ignore context when comparing replacements in case there are cases with the same context
        boolean ignoreContext =
            (replacements.size() != replacements.stream().map(IndexableReplacement::getContext).distinct().count()) ||
            (dbReplacements.size() != dbReplacements.stream().map(ReplacementEntity::getContext).distinct().count());
        replacements.forEach(
            replacement -> handleReplacement(replacement, dbReplacements, ignoreContext).ifPresent(result::add)
        );

        result.addAll(cleanUpPageReplacements(page, dbReplacements));

        LOGGER.debug("END Index list of replacements");

        return result;
    }

    /**
     * Check the given replacement with the ones in DB. Update the given DB list if needed.
     * @return The updated replacement to be managed in DB if needed.
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
            result = handleExistingReplacement(replacement, existing.get());
        } else {
            // New replacement
            ReplacementEntity newReplacement = convertToEntity(replacement);
            dbPageReplacements.add(newReplacement);
            result = Optional.of(newReplacement);
            LOGGER.debug("Replacement inserted in DB: {}", replacement);
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
     * @return The updated replacement to be managed in DB if needed, or empty if not.
     */
    private Optional<ReplacementEntity> handleExistingReplacement(
        IndexableReplacement replacement,
        ReplacementEntity dbReplacement
    ) {
        Optional<ReplacementEntity> result = Optional.empty();
        if (dbReplacement.isToBeReviewed() && dbReplacement.isOlderThan(replacement.getLastUpdate())) {
            dbReplacement.setToUpdate();
            dbReplacement.setLastUpdate(replacement.getLastUpdate());

            // Also update other values just in case any of them has changed
            dbReplacement.setPosition(replacement.getPosition());
            dbReplacement.setContext(replacement.getContext());

            result = Optional.of(dbReplacement);
        } else {
            dbReplacement.setToKeep();
        }
        return result;
    }

    private void saveReplacements(List<ReplacementEntity> replacements) {
        try {
            List<ReplacementEntity> toInsert = replacements
                .stream()
                .filter(ReplacementEntity::isToCreate)
                .collect(Collectors.toList());
            toInsert.forEach(r -> replacementDao.insert(r));

            List<ReplacementEntity> toUpdate = replacements
                .stream()
                .filter(ReplacementEntity::isToUpdate)
                .collect(Collectors.toList());
            toUpdate.forEach(r -> replacementDao.update(r));

            List<ReplacementEntity> toRemove = replacements
                .stream()
                .filter(ReplacementEntity::isToDelete)
                .collect(Collectors.toList());
            if (!toRemove.isEmpty()) {
                replacementDao.deleteAll(toRemove);
            }
        } catch (Exception e) {
            LOGGER.error("Error when saving replacements: {}", replacements, e);
        }
    }

    ReplacementEntity convertToEntity(IndexableReplacement replacement) {
        ReplacementEntity entity = modelMapper.map(replacement, ReplacementEntity.class);
        // It is mapping the page ID also to the entity Id
        entity.setId(null);
        entity.setLang(replacement.getLang().getCode());
        entity.setToCreate();
        return entity;
    }

    /**
     * Find obsolete replacements and add a dummy one if needed.
     *
     * @return A list of replacements to be managed in DB.
     */
    List<ReplacementEntity> cleanUpPageReplacements(IndexablePage page, List<ReplacementEntity> dbReplacements) {
        // We assume there are no custom replacements in the list
        List<ReplacementEntity> result = new ArrayList<>();

        // Find just in case the system-reviewed replacements and delete them
        List<ReplacementEntity> systemReviewed = dbReplacements
            .stream()
            .filter(rep -> rep.isSystemReviewed() && !rep.isDummy())
            .collect(Collectors.toList());
        systemReviewed.forEach(ReplacementEntity::setToDelete);
        result.addAll(systemReviewed);
        dbReplacements.removeAll(systemReviewed);

        // All remaining replacements to review and not checked so far are obsolete and thus to be deleted
        List<ReplacementEntity> obsolete = dbReplacements
            .stream()
            .filter(rep -> rep.isToBeReviewed() && StringUtils.isEmpty(rep.getCudAction()))
            .collect(Collectors.toList());
        obsolete.forEach(ReplacementEntity::setToDelete);
        result.addAll(obsolete);
        dbReplacements.removeAll(obsolete);

        // We use a dummy replacement to store in some place the last update of the page
        // in case there are no replacements to review to store it instead.
        // The user-reviewed replacements can't be used as they are only kept for the sake of statistics
        // and have the date of the user review action.

        // If there remain replacements to review there is no need of dummy replacement
        // If not a dummy replacement must be created or updated (if older)
        // As this is the last step there is no need to update the DB list
        boolean existReplacementsToReview = dbReplacements.stream().anyMatch(ReplacementEntity::isToBeReviewed);
        Optional<ReplacementEntity> dummy = dbReplacements.stream().filter(ReplacementEntity::isDummy).findAny();
        if (existReplacementsToReview) {
            if (dummy.isPresent()) {
                dummy.get().setToDelete();
                result.add(dummy.get());
            }
        } else {
            if (dummy.isPresent()) {
                if (dummy.get().isOlderThan(page.getLastUpdate())) {
                    dummy.get().setToUpdate();
                    dummy.get().setLastUpdate(page.getLastUpdate());
                    result.add(dummy.get());
                }
            } else {
                result.add(ReplacementEntity.createDummy(page.getId(), page.getLang(), page.getLastUpdate()));
            }
        }

        return result;
    }
}
