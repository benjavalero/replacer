package es.bvalero.replacer.replacement;

import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
    public static final String SYSTEM_REVIEWER = "system";

    @Autowired
    private ReplacementRepository replacementRepository;

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
            replacementRepository.findByPageIdAndLang(pageId, lang.getCode())
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

        // We need the page ID because the replacement list to index might be empty
        if (replacements.isEmpty() && dbReplacements.isEmpty()) {
            result.add(createFakeReviewedReplacement(pageId, lang));
        }

        replacements.forEach(replacement -> handleReplacement(replacement, dbReplacements).ifPresent(result::add));

        // Remove the remaining replacements
        dbReplacements
            .stream()
            .filter(ReplacementEntity::isToBeReviewed)
            .forEach(
                rep -> {
                    result.add(reviewReplacementAsSystem(rep));
                    LOGGER.debug("Replacement reviewed in DB with system: {}", rep);
                }
            );
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
        Optional<ReplacementEntity> result;
        if (dbReplacement.getLastUpdate().isBefore(replacement.getLastUpdate()) && dbReplacement.isToBeReviewed()) { // DB older than Dump
            dbReplacement.setLastUpdate(replacement.getLastUpdate());

            // Also update position and context in case any of them has changed
            dbReplacement.setPosition(replacement.getPosition());
            dbReplacement.setContext(replacement.getContext());
            LOGGER.debug("Replacement updated in DB: {}", dbReplacement);
            result = Optional.of(dbReplacement);
        } else {
            LOGGER.debug("Replacement existing in DB: {}", dbReplacement);
            result = Optional.empty();
        }
        return result;
    }

    private void saveReplacement(ReplacementEntity replacement) {
        try {
            replacementRepository.save(replacement);
        } catch (Exception e) {
            LOGGER.error("Error when saving replacement: {}", replacement, e);
        }
    }

    private void saveReplacements(List<ReplacementEntity> replacements) {
        try {
            replacementRepository.saveAll(replacements);
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
        return reviewReplacement(replacement, SYSTEM_REVIEWER);
    }

    public void reviewPageReplacementsAsSystem(int pageId, WikipediaLanguage lang) {
        replacementRepository
            .findByPageIdAndLangAndReviewerIsNull(pageId, lang.getCode())
            .forEach(r -> saveReplacement(reviewReplacementAsSystem(r)));
    }

    public void reviewPageReplacements(
        int pageId,
        WikipediaLanguage lang,
        @Nullable String type,
        @Nullable String subtype,
        String reviewer
    ) {
        LOGGER.info("START Mark page as reviewed. ID: {}", pageId);
        List<ReplacementEntity> toSave = new ArrayList<>();

        if (ReplacementFindService.CUSTOM_FINDER_TYPE.equals(type)) {
            // Custom replacements don't exist in the database to be reviewed
            toSave.add(createCustomReviewedReplacement(pageId, lang, subtype, reviewer));
        } else if (StringUtils.isNotBlank(type)) {
            toSave.addAll(reviewPageTypedReplacements(pageId, lang, type, subtype, reviewer));
        } else {
            toSave.addAll(reviewPageReplacements(pageId, lang, reviewer));
        }

        saveReplacements(toSave);
        LOGGER.info("END Mark page as reviewed. ID: {}", pageId);
    }

    private List<ReplacementEntity> reviewPageReplacements(int pageId, WikipediaLanguage lang, String reviewer) {
        return replacementRepository
            .findByPageIdAndLangAndReviewerIsNull(pageId, lang.getCode())
            .stream()
            .map(replacement -> reviewReplacement(replacement, reviewer))
            .collect(Collectors.toList());
    }

    private List<ReplacementEntity> reviewPageTypedReplacements(
        int pageId,
        WikipediaLanguage lang,
        String type,
        String subtype,
        String reviewer
    ) {
        List<ReplacementEntity> toReview = replacementRepository.findByPageIdAndLangAndTypeAndSubtypeAndReviewerIsNull(
            pageId,
            lang.getCode(),
            type,
            subtype
        );
        toReview.forEach(replacement -> reviewReplacement(replacement, reviewer));

        // Decrease the cached count for the replacement
        replacementCountService.decreaseCachedReplacementsCount(lang, type, subtype, toReview.size());

        return toReview;
    }

    private ReplacementEntity createCustomReviewedReplacement(
        int pageId,
        WikipediaLanguage lang,
        String replacement,
        String reviewer
    ) {
        ReplacementEntity custom = new ReplacementEntity(
            pageId,
            lang,
            ReplacementFindService.CUSTOM_FINDER_TYPE,
            replacement,
            0
        );
        return reviewReplacement(custom, reviewer);
    }

    public void addCustomReviewedReplacement(int pageId, WikipediaLanguage lang, String replacement) {
        ReplacementEntity customReplacement = new ReplacementEntity(
            pageId,
            lang,
            ReplacementFindService.CUSTOM_FINDER_TYPE,
            replacement,
            0
        );
        saveReplacement(reviewReplacementAsSystem(customReplacement));
    }

    ReplacementEntity createFakeReviewedReplacement(int pageId, WikipediaLanguage lang) {
        ReplacementEntity fakeReplacement = new ReplacementEntity(pageId, lang, "", "", 0);
        return reviewReplacementAsSystem(fakeReplacement);
    }
}
