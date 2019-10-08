package es.bvalero.replacer.article;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ArticleStatsService {

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Cache the count of replacements. This list is updated every 10 minutes and modified when saving changes.
    private ListValuedMap<String, SubtypeCount> cachedReplacementCount = new ArrayListValuedHashMap<>();

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

    List<ReviewerCount> countReplacementsGroupedByReviewer() {
        return replacementRepository.countGroupedByReviewer(ArticleIndexService.SYSTEM_REVIEWER);
    }

    /* LIST OF REPLACEMENTS */

    ListValuedMap<String, SubtypeCount> findMisspellingsGrouped() {
        return this.cachedReplacementCount;
    }

    /**
     * Update the count of misspellings from Wikipedia.
     */
    @Scheduled(fixedDelayString = "${replacer.article.stats.delay}")
    public void updateReplacementCount() {
        LOGGER.info("EXECUTE Scheduled update of grouped replacements count");
        LOGGER.info("START Count grouped replacements by type and subtype");
        List<TypeSubtypeCount> counts = replacementRepository.countGroupedByTypeAndSubtype();
        LOGGER.info("END Count grouped replacements. Size: {}", counts.size());

        this.cachedReplacementCount.clear();
        counts.forEach(count -> this.cachedReplacementCount.put(count.getType(), convertToDto(count)));
    }

    private SubtypeCount convertToDto(TypeSubtypeCount entity) {
        return modelMapper.map(entity, SubtypeCount.class);
    }

    void removeCachedReplacementCount(String type, String subtype) {
        this.cachedReplacementCount.get(type).removeIf(item -> item.getSubtype().equals(subtype));
    }

    void decreaseCachedReplacementsCount(String type, String subtype, int size) {
        this.cachedReplacementCount.get(type).stream()
                .filter(item -> item.getSubtype().equals(subtype))
                .findAny()
                .ifPresent(item -> item.decrementCount(size));

        // Clean the possible empty counts after decreasing
        this.cachedReplacementCount.get(type).removeIf(item -> item.getCount() <= 0);
    }

}
