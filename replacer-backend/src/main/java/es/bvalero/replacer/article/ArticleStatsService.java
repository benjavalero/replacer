package es.bvalero.replacer.article;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ArticleStatsService {

    @Autowired
    private ReplacementRepository replacementRepository;

    // Cache the count of replacements. This list is updated every 10 minutes and modified when saving changes.
    private List<ReplacementCount> cachedReplacementCount = new ArrayList<>();

    /* STATISTICS */

    long countReplacements() {
        return replacementRepository.countByReviewerIsNullOrReviewerIsNot(ArticleIndexService.SYSTEM_REVIEWER);
    }

    long countReplacementsReviewed() {
        return replacementRepository.countByReviewerIsNotNullAndReviewerIsNot(ArticleIndexService.SYSTEM_REVIEWER);
    }

    long countReplacementsToReview() {
        return replacementRepository.countByReviewerIsNull();
    }

    List<Object[]> countReplacementsGroupedByReviewer() {
        return replacementRepository.countGroupedByReviewer(ArticleIndexService.SYSTEM_REVIEWER);
    }

    /* LIST OF REPLACEMENTS */

    List<ReplacementCount> findMisspellingsGrouped() {
        return this.cachedReplacementCount;
    }

    /**
     * Update every 10 minutes the count of misspellings from Wikipedia
     */
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void updateReplacementCount() {
        LOGGER.info("EXECUTE Scheduled update of grouped replacements count");
        LOGGER.info("START Count grouped replacements");
        List<ReplacementCount> count = replacementRepository.findReplacementCountByTypeAndSubtype();
        LOGGER.info("END Count grouped replacements. Size: {}", count.size());

        this.cachedReplacementCount.clear();
        this.cachedReplacementCount.addAll(count);
    }

    void removeCachedReplacements(String type, String subtype) {
        this.cachedReplacementCount.removeIf(item -> item.getType().equals(type) && item.getSubtype().equals(subtype));
    }

    void decreaseCachedReplacementsCount(String type, String subtype, int size) {
        this.cachedReplacementCount.stream()
                .filter(item -> item.getType().equals(type) && item.getSubtype().equals(subtype))
                .findAny()
                .ifPresent(item -> item.decrementCount(size));
    }

}
