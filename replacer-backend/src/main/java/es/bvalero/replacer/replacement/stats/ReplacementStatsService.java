package es.bvalero.replacer.replacement.stats;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class ReplacementStatsService {

    @Autowired
    private ReplacementStatsRepository replacementStatsRepository;

    // Statistics caches
    // The queries in database can be heavy, so we preload the counts on start and refresh them periodically.
    // They are not used often so for the moment it is not worth to add synchronization.
    private final Map<WikipediaLanguage, Long> countReviewed = new EnumMap<>(WikipediaLanguage.class);
    private final Map<WikipediaLanguage, Long> countNotReviewed = new EnumMap<>(WikipediaLanguage.class);
    private final Map<WikipediaLanguage, Collection<ReviewerCount>> countByReviewer = new EnumMap<>(
        WikipediaLanguage.class
    );

    long countReplacementsReviewed(WikipediaLanguage lang) {
        return this.countReviewed.computeIfAbsent(
                lang,
                l -> this.replacementStatsRepository.countReplacementsReviewed(l)
            );
    }

    long countReplacementsNotReviewed(WikipediaLanguage lang) {
        return this.countNotReviewed.computeIfAbsent(
                lang,
                l -> this.replacementStatsRepository.countReplacementsNotReviewed(l)
            );
    }

    Collection<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang) {
        return this.countByReviewer.computeIfAbsent(
                lang,
                l -> this.replacementStatsRepository.countReplacementsGroupedByReviewer(l)
            );
    }

    @Scheduled(fixedDelayString = "${replacer.page.stats.delay}")
    public void scheduledUpdateStatistics() {
        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            this.countReviewed.put(lang, this.replacementStatsRepository.countReplacementsReviewed(lang));
            this.countNotReviewed.put(lang, this.replacementStatsRepository.countReplacementsNotReviewed(lang));
            this.countByReviewer.put(lang, this.replacementStatsRepository.countReplacementsGroupedByReviewer(lang));
        }
    }
}
