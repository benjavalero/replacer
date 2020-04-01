package es.bvalero.replacer.replacement;

import es.bvalero.replacer.finder.ReplacementFindService;
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

    /* INDEX ARTICLES */

    public void indexArticleReplacements(int articleId, List<IndexableReplacement> replacements) {
        // All replacements correspond to the same article
        if (replacements.stream().anyMatch(r -> r.getArticleId() != articleId)) {
            throw new IllegalArgumentException("Indexable replacements from more than one article");
        }

        // We need the article ID because the replacement list to index might be empty
        List<ReplacementEntity> toSave = findIndexArticleReplacements(
            articleId,
            replacements,
            replacementRepository.findByArticleId(articleId)
        );
        saveReplacements(toSave);
    }

    public List<ReplacementEntity> findIndexArticleReplacements(
        int articleId,
        List<IndexableReplacement> replacements,
        List<ReplacementEntity> dbReplacements
    ) {
        LOGGER.debug(
            "START Index list of replacements. ID: {}\n" + "New: {} - {}\n" + "Old: {} - {}",
            articleId,
            replacements.size(),
            replacements,
            dbReplacements.size(),
            dbReplacements
        );
        List<ReplacementEntity> result = new ArrayList<>();

        // We need the article ID because the replacement list to index might be empty
        if (replacements.isEmpty() && dbReplacements.isEmpty()) {
            result.add(createFakeReviewedReplacement(articleId));
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
        List<ReplacementEntity> dbArticleReplacements
    ) {
        Optional<ReplacementEntity> result;
        Optional<ReplacementEntity> existing = findSameReplacementInCollection(replacement, dbArticleReplacements);
        if (existing.isPresent()) {
            result = handleExistingReplacement(replacement, existing.get());
            dbArticleReplacements.remove(existing.get());
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
            replacement.getArticleId() == entity.getArticleId() &&
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
        // It is mapping the articleId also to the entity Id
        entity.setId(null);
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

    public void reviewArticleReplacementsAsSystem(int articleId) {
        replacementRepository
            .findByArticleIdAndReviewerIsNull(articleId)
            .forEach(r -> saveReplacement(reviewReplacementAsSystem(r)));
    }

    public void reviewArticleReplacements(
        int articleId,
        @Nullable String type,
        @Nullable String subtype,
        String reviewer
    ) {
        LOGGER.info("START Mark article as reviewed. ID: {}", articleId);
        List<ReplacementEntity> toSave = new ArrayList<>();

        if (ReplacementFindService.CUSTOM_FINDER_TYPE.equals(type)) {
            // Custom replacements don't exist in the database to be reviewed
            toSave.add(createCustomReviewedReplacement(articleId, subtype, reviewer));
        } else if (StringUtils.isNotBlank(type)) {
            toSave.addAll(reviewArticleTypedReplacements(articleId, type, subtype, reviewer));
        } else {
            toSave.addAll(reviewArticleReplacements(articleId, reviewer));
        }

        saveReplacements(toSave);
        LOGGER.info("END Mark article as reviewed. ID: {}", articleId);
    }

    private List<ReplacementEntity> reviewArticleReplacements(int articleId, String reviewer) {
        return replacementRepository
            .findByArticleIdAndReviewerIsNull(articleId)
            .stream()
            .map(replacement -> reviewReplacement(replacement, reviewer))
            .collect(Collectors.toList());
    }

    private List<ReplacementEntity> reviewArticleTypedReplacements(
        int articleId,
        String type,
        String subtype,
        String reviewer
    ) {
        List<ReplacementEntity> toReview = replacementRepository.findByArticleIdAndTypeAndSubtypeAndReviewerIsNull(
            articleId,
            type,
            subtype
        );
        toReview.forEach(replacement -> reviewReplacement(replacement, reviewer));

        // Decrease the cached count for the replacement
        replacementCountService.decreaseCachedReplacementsCount(type, subtype, toReview.size());

        return toReview;
    }

    private ReplacementEntity createCustomReviewedReplacement(int articleId, String replacement, String reviewer) {
        ReplacementEntity custom = new ReplacementEntity(
            articleId,
            ReplacementFindService.CUSTOM_FINDER_TYPE,
            replacement,
            0
        );
        return reviewReplacement(custom, reviewer);
    }

    public void addCustomReviewedReplacement(int articleId, String replacement) {
        ReplacementEntity customReplacement = new ReplacementEntity(
            articleId,
            ReplacementFindService.CUSTOM_FINDER_TYPE,
            replacement,
            0
        );
        saveReplacement(reviewReplacementAsSystem(customReplacement));
    }

    ReplacementEntity createFakeReviewedReplacement(int articleId) {
        ReplacementEntity fakeReplacement = new ReplacementEntity(articleId, "", "", 0);
        return reviewReplacementAsSystem(fakeReplacement);
    }
}
