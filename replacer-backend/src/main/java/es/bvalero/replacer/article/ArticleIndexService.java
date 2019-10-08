package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.Replacement;
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
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleIndexService {

    static final String SYSTEM_REVIEWER = "system";

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private ArticleStatsService articleStatsService;

    @Autowired
    private ModelMapper modelMapper;

    /* INDEX ARTICLES */

    void indexArticleReplacements(IndexableArticle article, List<Replacement> replacements) {
        LOGGER.debug("START Index replacements for article: {} - {}", article.getId(), article.getTitle());
        indexArticleReplacements(article, replacements, replacementRepository.findByArticleId(article.getId()));
        LOGGER.debug("END Index replacements for article: {} - {}", article.getId(), article.getTitle());
    }

    public void indexArticleReplacements(IndexableArticle article, List<Replacement> replacements, List<ReplacementEntity> dbReplacements) {
        List<IndexableReplacement> indexableReplacements = replacements.stream()
                .map(rep -> convertToDto(article, rep)).collect(Collectors.toList());

        // Trick: In case of no replacements found we insert a fake reviewed replacement
        // in order to be able to skip the article when reindexing
        if (replacements.isEmpty() && dbReplacements.isEmpty()) {
            ReplacementEntity newReplacement = new ReplacementEntity(article.getId(), "", "", 0);
            reviewReplacementAsSystem(newReplacement);
        }

        indexReplacements(indexableReplacements, dbReplacements);
    }

    private IndexableReplacement convertToDto(IndexableArticle article, Replacement replacement) {
        IndexableReplacement indexableReplacement = modelMapper.map(replacement, IndexableReplacement.class);
        indexableReplacement.setArticleId(article.getId());
        indexableReplacement.setPosition(replacement.getStart());
        indexableReplacement.setLastUpdate(article.getLastUpdate().toLocalDate());
        return indexableReplacement;
    }

    void indexReplacements(List<IndexableReplacement> replacements, List<ReplacementEntity> dbReplacements) {
        LOGGER.debug("START Index list of replacements\n" +
                        "New: {} - {}\n" +
                        "Old: {} - {}",
                replacements.size(), replacements,
                dbReplacements.size(), dbReplacements);

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
        return entities.stream().filter(entity -> isSame(replacement, entity)).findAny();
    }

    private boolean isSame(IndexableReplacement replacement, ReplacementEntity entity) {
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
        replacementRepository.save(replacement);
    }

    void insertReplacement(IndexableReplacement replacement) {
        replacementRepository.save(convertToEntity(replacement));
    }

    private ReplacementEntity convertToEntity(IndexableReplacement replacement) {
        return modelMapper.map(replacement, ReplacementEntity.class);
    }

    private void reviewReplacement(ReplacementEntity replacement, String reviewer) {
        replacement.setReviewer(reviewer);
        replacement.setLastUpdate(LocalDate.now());
        saveReplacement(replacement);
    }

    void reviewReplacementAsSystem(ReplacementEntity replacement) {
        replacement.setReviewer(SYSTEM_REVIEWER);
        replacement.setLastUpdate(LocalDate.now());
        saveReplacement(replacement);
    }

    void reviewArticleAsSystem(int articleId) {
        replacementRepository.findByArticleIdAndReviewerIsNull(articleId).forEach(this::reviewReplacementAsSystem);
    }

    void reviewArticlesAsSystem(Collection<Integer> articleIds) {
        articleIds.forEach(this::reviewArticleAsSystem);
    }

    void reviewArticle(int articleId, @Nullable String type, @Nullable String subtype, String reviewer) {
        LOGGER.info("START Mark article as reviewed. ID: {}", articleId);

        if (ReplacementFinderService.CUSTOM_FINDER_TYPE.equals(type)) {
            // Custom replacements don't exist in the database to be reviewed
            ReplacementEntity custom = new ReplacementEntity(articleId, type, subtype, 0);
            reviewReplacement(custom, reviewer);
        } else if (StringUtils.isNotBlank(subtype)) {
            List<ReplacementEntity> toReview = replacementRepository.findByArticleIdAndTypeAndSubtypeAndReviewerIsNull(
                    articleId, type, subtype);
            toReview.forEach(replacement -> reviewReplacement(replacement, reviewer));

            // Decrease the cached count for the replacement
            articleStatsService.decreaseCachedReplacementsCount(type, subtype, toReview.size());
        } else {
            replacementRepository.findByArticleIdAndReviewerIsNull(articleId)
                    .forEach(replacement -> reviewReplacement(replacement, reviewer));
        }

        LOGGER.info("END Mark article as reviewed. ID: {}", articleId);
    }

}
