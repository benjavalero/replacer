package es.bvalero.replacer.replacement.stats;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.ReplacementStatsRepository;
import es.bvalero.replacer.repository.ResultCount;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Implementation of the replacement repository which maintains a cache of the replacement stats */
@Primary
@Transactional
@Repository
class ReplacementStatsCacheRepository implements ReplacementStatsRepository {

    @Autowired
    @Qualifier("replacementJdbcRepository")
    private ReplacementStatsRepository replacementStatsRepository;

    // Statistics caches
    // The queries in database can be heavy, so we preload the counts on start and refresh them periodically.
    // They are not used often so for the moment it is not worth to add synchronization.
    private final Map<WikipediaLanguage, Integer> countReviewed = new EnumMap<>(WikipediaLanguage.class);
    private final Map<WikipediaLanguage, Integer> countNotReviewed = new EnumMap<>(WikipediaLanguage.class);
    private final Map<WikipediaLanguage, Collection<ResultCount<String>>> countByReviewer = new EnumMap<>(
        WikipediaLanguage.class
    );

    @Override
    public int countReplacementsReviewed(WikipediaLanguage lang) {
        return this.countReviewed.computeIfAbsent(
                lang,
                l -> this.replacementStatsRepository.countReplacementsReviewed(l)
            );
    }

    @Override
    public int countReplacementsNotReviewed(WikipediaLanguage lang) {
        return this.countNotReviewed.computeIfAbsent(
                lang,
                l -> this.replacementStatsRepository.countReplacementsNotReviewed(l)
            );
    }

    @Override
    public Collection<ResultCount<String>> countReplacementsByReviewer(WikipediaLanguage lang) {
        return this.countByReviewer.computeIfAbsent(
                lang,
                l -> this.replacementStatsRepository.countReplacementsByReviewer(l)
            );
    }

    // Add a delay of 1 minute so these queries don't overlap with the one to count replacement types
    @Loggable(value = LogLevel.DEBUG, skipArgs = true, skipResult = true)
    @Scheduled(fixedDelayString = "${replacer.page.stats.delay}", initialDelay = 60000)
    public void scheduledUpdateStatistics() {
        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            this.countReviewed.put(lang, this.replacementStatsRepository.countReplacementsReviewed(lang));
            this.countNotReviewed.put(lang, this.replacementStatsRepository.countReplacementsNotReviewed(lang));
            this.countByReviewer.put(lang, this.replacementStatsRepository.countReplacementsByReviewer(lang));
        }
    }
}
