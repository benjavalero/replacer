package es.bvalero.replacer.replacement;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReplacementCountService {

    @Autowired
    private ReplacementRepository replacementRepository;

    // Cache the count of replacements. This list is updated every 10 minutes and modified when saving changes.
    private Map<String, Map<String, Long>> cachedReplacementCount = new HashMap<>();

    /* STATISTICS */

    long countAllReplacements() {
        return replacementRepository.countByReviewerIsNullOrReviewerIsNot(ReplacementIndexService.SYSTEM_REVIEWER);
    }

    long countReplacementsReviewed() {
        return replacementRepository.countByReviewerIsNotNullAndReviewerIsNot(ReplacementIndexService.SYSTEM_REVIEWER);
    }

    long countReplacementsToReview() {
        return replacementRepository.countByReviewerIsNull();
    }

    List<ReviewerCount> countReplacementsGroupedByReviewer() {
        return replacementRepository.countGroupedByReviewer(ReplacementIndexService.SYSTEM_REVIEWER);
    }

    /* LIST OF REPLACEMENTS */

    List<TypeCount> findReplacementCount() {
        return convertToDto(this.cachedReplacementCount);
    }

    @TestOnly
    Map<String, Map<String, Long>> getCachedReplacementCount() {
        return this.cachedReplacementCount;
    }

    private List<TypeCount> convertToDto(Map<String, Map<String, Long>> map) {
        return map.entrySet().stream()
                .map(t -> TypeCount.of(t.getKey(),
                        t.getValue().entrySet().stream()
                                .map(x -> SubtypeCount.of(x.getKey(), x.getValue()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    /**
     * Update the count of misspellings from Wikipedia.
     */
    @Scheduled(fixedDelayString = "${replacer.article.stats.delay}")
    void updateReplacementCount() {
        LOGGER.info("EXECUTE Scheduled update of grouped replacements count");
        LOGGER.info("START Count grouped replacements by type and subtype");
        List<TypeSubtypeCount> counts = replacementRepository.countGroupedByTypeAndSubtype();
        LOGGER.info("END Count grouped replacements. Size: {}", counts.size());
        loadCachedReplacementCount(counts);
    }

    private void loadCachedReplacementCount(List<TypeSubtypeCount> counts) {
        this.cachedReplacementCount.clear();
        for (TypeSubtypeCount count : counts) {
            if (!this.cachedReplacementCount.containsKey(count.getType())) {
                this.cachedReplacementCount.put(count.getType(), new HashMap<>());
            }
            this.cachedReplacementCount.get(count.getType()).put(count.getSubtype(), count.getCount());
        }
    }

    public void removeCachedReplacementCount(String type, String subtype) {
        if (this.cachedReplacementCount.containsKey(type)) {
            this.cachedReplacementCount.get(type).remove(subtype);
            if (this.cachedReplacementCount.get(type).isEmpty()) {
                this.cachedReplacementCount.remove(type);
            }
        }
    }

    void decreaseCachedReplacementsCount(String type, String subtype, int size) {
        long currentCount = this.cachedReplacementCount.get(type).get(subtype);
        long newCount = currentCount - size;
        if (newCount > 0) {
            this.cachedReplacementCount.get(type).put(subtype, newCount);
        } else {
            // Clean the possible empty counts after decreasing
            removeCachedReplacementCount(type, subtype);
        }
    }

}
