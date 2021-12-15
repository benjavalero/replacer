package es.bvalero.replacer.replacement.stats;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.ReplacementStatsRepository;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** Service to retrieve and cache the replacement counts for statistics */
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
        return this.countByReviewer.computeIfAbsent(lang, this::findReplacementCountsByReviewer);
    }

    private Collection<ReviewerCount> findReplacementCountsByReviewer(WikipediaLanguage lang) {
        return ReviewerCountMapper.fromModel(this.replacementStatsRepository.countReplacementsByReviewer(lang));
    }

    @Loggable(value = LogLevel.DEBUG, skipArgs = true, skipResult = true)
    @Scheduled(fixedDelayString = "${replacer.page.stats.delay}")
    public void scheduledUpdateStatistics() {
        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            this.countReviewed.put(lang, this.replacementStatsRepository.countReplacementsReviewed(lang));
            this.countNotReviewed.put(lang, this.replacementStatsRepository.countReplacementsNotReviewed(lang));
            this.countByReviewer.put(lang, this.findReplacementCountsByReviewer(lang));
        }
    }
}
