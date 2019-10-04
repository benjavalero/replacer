package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleIndexService {

    static final String SYSTEM_REVIEWER = "system";

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private ArticleStatsService articleStatsService;

    /* INDEX ARTICLES */

    void indexArticleReplacements(WikipediaPage article, Collection<Replacement> replacements) {
        LOGGER.debug("START Index replacements for article: {} - {}", article.getId(), article.getTitle());
        indexReplacements(article,
                convertArticleReplacements(article, replacements),
                replacementRepository.findByArticleId(article.getId())
        );
        LOGGER.debug("END Index replacements for article: {} - {}", article.getId(), article.getTitle());
    }

    public void indexArticleReplacements(WikipediaPage article, Collection<Replacement> replacements,
                                         Collection<ReplacementEntity> dbReplacements) {
        LOGGER.debug("START Index replacements for article: {} - {}", article.getId(), article.getTitle());
        indexReplacements(article,
                convertArticleReplacements(article, replacements),
                dbReplacements
        );
        LOGGER.debug("END Index replacements for article: {} - {}", article.getId(), article.getTitle());
    }

    void indexReplacements(WikipediaPage article, Collection<ReplacementEntity> replacements,
                           Collection<ReplacementEntity> dbReplacements) {
        LOGGER.debug("START Index list of replacements\n" +
                        "New: {} - {}\n" +
                        "Old: {} - {}",
                replacements.size(), replacements,
                dbReplacements.size(), dbReplacements);

        // Trick: In case of no replacements found we insert a fake reviewed replacement
        // in order to be able to skip the article when reindexing
        if (replacements.isEmpty() && dbReplacements.isEmpty()) {
            ReplacementEntity newReplacement = new ReplacementEntity(article.getId(), "", "", 0);
            reviewReplacementAsSystem(newReplacement);
        }

        replacements.forEach(replacement -> {
            Optional<ReplacementEntity> existing = findSameReplacementInCollection(replacement, dbReplacements);
            if (existing.isPresent()) {
                handleExistingReplacement(replacement, existing.get());
                dbReplacements.remove(existing.get());
            } else {
                // New replacement
                saveReplacement(replacement);
                LOGGER.debug("Replacement inserted in DB: {}", replacement);
            }
        });

        // Remove the remaining replacements
        dbReplacements.stream().filter(ReplacementEntity::isToBeReviewed).forEach(rep -> {
            reviewReplacementAsSystem(rep);
            LOGGER.debug("Replacement reviewed in DB with system: {}", rep);
        });
        LOGGER.debug("END Index list of replacements");
    }

    private Optional<ReplacementEntity> findSameReplacementInCollection(ReplacementEntity replacement,
                                                                        Collection<ReplacementEntity> replacements) {
        return replacements.stream().filter(rep -> rep.isSame(replacement)).findAny();
    }

    private Collection<ReplacementEntity> convertArticleReplacements(WikipediaPage article,
                                                                     Collection<Replacement> replacements) {
        return replacements.stream().map(
                replacement -> new ReplacementEntity(
                        article.getId(), replacement.getType(), replacement.getSubtype(),
                        replacement.getStart())
                        .withLastUpdate(article.getLastUpdate().toLocalDate()))
                .collect(Collectors.toList());
    }

    private void handleExistingReplacement(ReplacementEntity newReplacement, ReplacementEntity dbReplacement) {
        if (dbReplacement.getLastUpdate().isBefore(newReplacement.getLastUpdate())
                && dbReplacement.isToBeReviewed()) { // DB older than Dump
            ReplacementEntity updated = dbReplacement.withLastUpdate(newReplacement.getLastUpdate());
            saveReplacement(updated);
            LOGGER.debug("Replacement updated in DB: {}", updated);
        } else {
            LOGGER.debug("Replacement existing in DB: {}", dbReplacement);
        }
    }

    private void saveReplacement(ReplacementEntity replacement) {
        replacementRepository.save(replacement);
    }

    private void reviewReplacement(ReplacementEntity replacement, String reviewer) {
        saveReplacement(replacement.withReviewer(reviewer).withLastUpdate(LocalDate.now()));
    }

    void reviewReplacementAsSystem(ReplacementEntity replacement) {
        saveReplacement(replacement.withReviewer(SYSTEM_REVIEWER).withLastUpdate(LocalDate.now()));
    }

    void reviewArticleAsSystem(int articleId) {
        replacementRepository.findByArticleIdAndReviewerIsNull(articleId).forEach(this::reviewReplacementAsSystem);
    }

    public void reviewArticlesAsSystem(Set<Integer> articleIds) {
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
