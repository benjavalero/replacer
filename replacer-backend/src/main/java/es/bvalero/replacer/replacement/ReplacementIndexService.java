package es.bvalero.replacer.replacement;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReplacementIndexService {
    @Autowired
    private ReplacementDao replacementDao;

    @Autowired
    private ReplacementCountService replacementCountService;

    @Autowired
    private ModelMapper modelMapper;

    /* INDEX PAGES */

    /**
     * Add a list of replacements for a page to the database. In case the page is already indexed,
     * the existing replacements in the database are updated. The ID is needed in case the list is empty
     * so we can still update the existing replacements to mark them as obsolete.
     */
    public void indexPageReplacements(int pageId, WikipediaLanguage lang, List<IndexableReplacement> replacements) {
        // All replacements correspond to the same page
        if (replacements.stream().anyMatch(r -> r.getPageId() != pageId)) {
            throw new IllegalArgumentException("Indexable replacements from more than one page");
        }

        // We need the page ID because the replacement list to index might be empty
        List<ReplacementEntity> toSave = findIndexPageReplacements(
            pageId,
            lang,
            replacements,
            replacementDao.findByPageId(pageId, lang)
        );
        saveReplacements(toSave);
    }

    /**
     * Add a list of replacements for a page to the database, updating the existing replacements in the database,
     * received also as a parameter. The ID is needed in case the list is empty
     * so we can still update the existing replacements to mark them as obsolete.
     *
     * This method should be private but in cases like dump indexing it is worth to provide the DB replacements directly.
     */
    public List<ReplacementEntity> findIndexPageReplacements(
        int pageId,
        WikipediaLanguage lang,
        List<IndexableReplacement> replacements,
        List<ReplacementEntity> dbReplacements
    ) {
        LOGGER.debug(
            "START Index list of replacements. ID: {} - {}\n" + "New: {} - {}\n" + "Old: {} - {}",
            pageId,
            lang,
            replacements.size(),
            replacements,
            dbReplacements.size(),
            dbReplacements
        );
        List<ReplacementEntity> result = new ArrayList<>();

        replacements.forEach(replacement -> handleReplacement(replacement, dbReplacements).ifPresent(result::add));

        // Remove the remaining replacements not reviewed
        List<ReplacementEntity> toRemove = dbReplacements
            .stream()
            .filter(rep -> !rep.isCustom())
            .filter(rep -> !rep.isUserReviewed())
            .collect(Collectors.toList());
        dbReplacements.removeAll(toRemove);

        // In case there are no replacements to be added to the DB,
        // the page would be parsed again in the next indexation even if it is not needed
        // so we add a fake row for the page to set the date of the last update
        if (result.isEmpty() && dbReplacements.isEmpty()) {
            result.add(createFakeReviewedReplacement(pageId, lang));
        }

        result.addAll(setToDelete(toRemove));
        LOGGER.debug("END Index list of replacements");

        return result;
    }

    private Optional<ReplacementEntity> handleReplacement(
        IndexableReplacement replacement,
        List<ReplacementEntity> dbPageReplacements
    ) {
        Optional<ReplacementEntity> result;
        Optional<ReplacementEntity> existing = findSameReplacementInCollection(replacement, dbPageReplacements);
        if (existing.isPresent()) {
            result = handleExistingReplacement(replacement, existing.get());
            dbPageReplacements.remove(existing.get());
        } else {
            // New replacement
            result = Optional.of(convertToEntity(replacement));
            LOGGER.debug("Replacement inserted in DB: {}", replacement);
        }
        return result;
    }

    private Optional<ReplacementEntity> findSameReplacementInCollection(
        IndexableReplacement replacement,
        Collection<ReplacementEntity> entities
    ) {
        return entities.stream().filter(entity -> isSameReplacement(replacement, entity)).findAny();
    }

    private boolean isSameReplacement(IndexableReplacement replacement, ReplacementEntity entity) {
        return (
            replacement.getPageId() == entity.getPageId() &&
            replacement.getType().equals(entity.getType()) &&
            replacement.getSubtype().equals(entity.getSubtype()) &&
            (replacement.getPosition() == entity.getPosition() || replacement.getContext().equals(entity.getContext()))
        );
    }

    private Optional<ReplacementEntity> handleExistingReplacement(
        IndexableReplacement replacement,
        ReplacementEntity dbReplacement
    ) {
        Optional<ReplacementEntity> result = Optional.empty();
        if (dbReplacement.isToBeReviewed()) {
            if (dbReplacement.getLastUpdate().isBefore(replacement.getLastUpdate())) {
                // DB older than Dump
                dbReplacement.setLastUpdate(replacement.getLastUpdate());

                // Also update other values in case any of them has changed
                dbReplacement.setPosition(replacement.getPosition());
                dbReplacement.setContext(replacement.getContext());
                dbReplacement.setTitle(replacement.getTitle());

                result = Optional.of(dbReplacement);
            } else if (
                replacement.getPosition() != dbReplacement.getPosition() ||
                !replacement.getContext().equals(dbReplacement.getContext())
            ) {
                // There is no need to check the title as we don't retrieve it and it never changes
                // Also update other values in case any of them has changed
                dbReplacement.setPosition(replacement.getPosition());
                dbReplacement.setContext(replacement.getContext());
                dbReplacement.setTitle(replacement.getTitle());

                result = Optional.of(dbReplacement);
            }
        }
        return result;
    }

    private void saveReplacement(ReplacementEntity replacement) {
        try {
            replacementDao.insert(replacement);
        } catch (Exception e) {
            LOGGER.error("Error when saving replacement: {}", replacement, e);
        }
    }

    private void saveReplacements(List<ReplacementEntity> replacements) {
        try {
            List<ReplacementEntity> toInsert = replacements
                .stream()
                .filter(ReplacementEntity::isToInsert)
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
        return entity;
    }

    private ReplacementEntity reviewReplacement(ReplacementEntity replacement, String reviewer) {
        replacement.setReviewer(reviewer);
        replacement.setLastUpdate(LocalDate.now());
        return replacement;
    }

    private ReplacementEntity reviewReplacementAsSystem(ReplacementEntity replacement) {
        return reviewReplacement(replacement, ReplacementEntity.REVIEWER_SYSTEM);
    }

    public void reviewPageReplacementsAsSystem(int pageId, WikipediaLanguage lang) {
        replacementDao.reviewByPageId(lang, pageId, null, null, ReplacementEntity.REVIEWER_SYSTEM);
    }

    public void reviewPageReplacements(
        int pageId,
        WikipediaLanguage lang,
        @Nullable String type,
        @Nullable String subtype,
        String reviewer
    ) {
        LOGGER.info("START Mark page as reviewed. ID: {}", pageId);

        if (ReplacementEntity.TYPE_CUSTOM.equals(type)) {
            // Custom replacements don't exist in the database to be reviewed
            ReplacementEntity customReviewed = createCustomReviewedReplacement(pageId, lang, subtype, reviewer);
            saveReplacement(customReviewed);
        } else if (StringUtils.isNotBlank(type)) {
            replacementDao.reviewByPageId(lang, pageId, type, subtype, reviewer);

            // Decrease the cached count (one page)
            replacementCountService.decreaseCachedReplacementsCount(lang, type, subtype, 1);
        } else {
            replacementDao.reviewByPageId(lang, pageId, null, null, reviewer);
        }

        LOGGER.info("END Mark page as reviewed. ID: {}", pageId);
    }

    ReplacementEntity createCustomReviewedReplacement(
        int pageId,
        WikipediaLanguage lang,
        String replacement,
        String reviewer
    ) {
        ReplacementEntity custom = new ReplacementEntity(pageId, lang, ReplacementEntity.TYPE_CUSTOM, replacement, 0);
        return reviewReplacement(custom, reviewer);
    }

    public void addCustomReviewedReplacement(int pageId, WikipediaLanguage lang, String replacement) {
        ReplacementEntity customReplacement = new ReplacementEntity(
            pageId,
            lang,
            ReplacementEntity.TYPE_CUSTOM,
            replacement,
            0
        );
        saveReplacement(reviewReplacementAsSystem(customReplacement));
    }

    ReplacementEntity createFakeReviewedReplacement(int pageId, WikipediaLanguage lang) {
        ReplacementEntity fakeReplacement = new ReplacementEntity(pageId, lang, "", "", 0);
        return reviewReplacementAsSystem(fakeReplacement);
    }

    private List<ReplacementEntity> setToDelete(List<ReplacementEntity> replacements) {
        return replacements.stream().map(this::setToDelete).collect(Collectors.toList());
    }

    ReplacementEntity setToDelete(ReplacementEntity replacement) {
        replacement.setType(ReplacementEntity.TYPE_DELETE);
        return replacement;
    }
}
