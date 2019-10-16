package es.bvalero.replacer.replacement;

import es.bvalero.replacer.finder.ReplacementFinderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ReplacementIndexService {

    static final String SYSTEM_REVIEWER = "system";

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
        indexArticleReplacements(articleId, replacements, replacementRepository.findByArticleId(articleId));
    }

    public void indexArticleReplacements(
            int articleId, List<IndexableReplacement> replacements, List<ReplacementEntity> dbReplacements) {
        LOGGER.debug("START Index list of replacements. ID: {}\n" +
                        "New: {} - {}\n" +
                        "Old: {} - {}",
                articleId,
                replacements.size(), replacements,
                dbReplacements.size(), dbReplacements);

        // We need the article ID because the replacement list to index might be empty
        if (replacements.isEmpty() && dbReplacements.isEmpty()) {
            addFakeReviewedReplacement(articleId);
        }

        replacements.forEach(replacement -> indexReplacement(replacement, dbReplacements));

        // Remove the remaining replacements
        dbReplacements.stream().filter(ReplacementEntity::isToBeReviewed).forEach(rep -> {
            reviewReplacementAsSystem(rep);
            LOGGER.debug("Replacement reviewed in DB with system: {}", rep);
        });
        LOGGER.debug("END Index list of replacements");
    }

    private void indexReplacement(IndexableReplacement replacement, List<ReplacementEntity> dbArticleReplacements) {
        Optional<ReplacementEntity> existing = findSameReplacementInCollection(replacement, dbArticleReplacements);
        if (existing.isPresent()) {
            handleExistingReplacement(replacement, existing.get());
            dbArticleReplacements.remove(existing.get());
        } else {
            // New replacement
            insertReplacement(replacement);
            LOGGER.debug("Replacement inserted in DB: {}", replacement);
        }
    }

    private Optional<ReplacementEntity> findSameReplacementInCollection(
            IndexableReplacement replacement, Collection<ReplacementEntity> entities) {
        return entities.stream().filter(entity -> isSameReplacement(replacement, entity)).findAny();
    }

    private boolean isSameReplacement(IndexableReplacement replacement, ReplacementEntity entity) {
        return replacement.getArticleId() == entity.getArticleId() &&
                replacement.getType().equals(entity.getType()) &&
                replacement.getSubtype().equals(entity.getSubtype()) &&
                replacement.getPosition() == entity.getPosition();
    }

    private void handleExistingReplacement(IndexableReplacement replacement, ReplacementEntity dbReplacement) {
        if (dbReplacement.getLastUpdate().isBefore(replacement.getLastUpdate())
                && dbReplacement.isToBeReviewed()) { // DB older than Dump
            dbReplacement.setLastUpdate(replacement.getLastUpdate());
            saveReplacement(dbReplacement);
            LOGGER.debug("Replacement updated in DB: {}", dbReplacement);
        } else {
            LOGGER.debug("Replacement existing in DB: {}", dbReplacement);
        }
    }

    private void saveReplacement(ReplacementEntity replacement) {
        try {
            replacementRepository.save(replacement);
        } catch (Exception e) {
            LOGGER.error("Error when saving replacement: {}", replacement, e);
        }
    }

    void insertReplacement(IndexableReplacement replacement) {
        saveReplacement(convertToEntity(replacement));
    }

    private ReplacementEntity convertToEntity(IndexableReplacement replacement) {
        return modelMapper.map(replacement, ReplacementEntity.class);
    }

    private void reviewReplacement(ReplacementEntity replacement, String reviewer) {
        replacement.setReviewer(reviewer);
        replacement.setLastUpdate(LocalDate.now());
        saveReplacement(replacement);
    }

    private void reviewReplacementAsSystem(ReplacementEntity replacement) {
        replacement.setReviewer(SYSTEM_REVIEWER);
        replacement.setLastUpdate(LocalDate.now());
        saveReplacement(replacement);
    }

    public void reviewArticleReplacementsAsSystem(int articleId) {
        replacementRepository.findByArticleIdAndReviewerIsNull(articleId).forEach(this::reviewReplacementAsSystem);
    }

    public void reviewArticlesReplacementsAsSystem(Collection<Integer> articleIds) {
        articleIds.forEach(this::reviewArticleReplacementsAsSystem);
    }

    public void reviewArticleReplacements(int articleId, @Nullable String type, @Nullable String subtype, String reviewer) {
        LOGGER.info("START Mark article as reviewed. ID: {}", articleId);

        if (ReplacementFinderService.CUSTOM_FINDER_TYPE.equals(type)) {
            // Custom replacements don't exist in the database to be reviewed
            addCustomReviewedReplacement(articleId, subtype, reviewer);
        } else if (StringUtils.isNotBlank(type)) {
            reviewArticleTypedReplacements(articleId, type, subtype, reviewer);
        } else {
            reviewArticleReplacements(articleId, reviewer);
        }

        LOGGER.info("END Mark article as reviewed. ID: {}", articleId);
    }

    private void reviewArticleReplacements(int articleId, String reviewer) {
        replacementRepository.findByArticleIdAndReviewerIsNull(articleId)
                .forEach(replacement -> reviewReplacement(replacement, reviewer));
    }

    private void reviewArticleTypedReplacements(int articleId, String type, String subtype, String reviewer) {
        List<ReplacementEntity> toReview = replacementRepository.findByArticleIdAndTypeAndSubtypeAndReviewerIsNull(
                articleId, type, subtype);
        toReview.forEach(replacement -> reviewReplacement(replacement, reviewer));

        // Decrease the cached count for the replacement
        replacementCountService.decreaseCachedReplacementsCount(type, subtype, toReview.size());
    }

    private void addCustomReviewedReplacement(int articleId, String replacement, String reviewer) {
        ReplacementEntity custom = new ReplacementEntity(articleId, ReplacementFinderService.CUSTOM_FINDER_TYPE, replacement, 0);
        reviewReplacement(custom, reviewer);
    }

    public void addCustomReviewedReplacement(int articleId, String replacement) {
        ReplacementEntity customReplacement = new ReplacementEntity(articleId, ReplacementFinderService.CUSTOM_FINDER_TYPE, replacement, 0);
        reviewReplacementAsSystem(customReplacement);
    }

    void addFakeReviewedReplacement(int articleId) {
        ReplacementEntity fakeReplacement = new ReplacementEntity(articleId, "", "", 0);
        reviewReplacementAsSystem(fakeReplacement);
    }

}
