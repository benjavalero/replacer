package es.bvalero.replacer.replacement.stats;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
class ReplacementStatsDaoProxy implements ReplacementStatsDao {

    @Autowired
    private ReplacementStatsDao replacementStatsDao;

    // Statistics caches
    // The queries in database can be heavy so we preload the counts on start and refresh them periodically.
    // They are not used often so for the moment it is not worth to add synchronization.
    private final Map<WikipediaLanguage, Long> countReviewed = new EnumMap<>(WikipediaLanguage.class);
    private final Map<WikipediaLanguage, Long> countNotReviewed = new EnumMap<>(WikipediaLanguage.class);
    private final Map<WikipediaLanguage, List<ReviewerCount>> countByReviewer = new EnumMap<>(WikipediaLanguage.class);

    /* STATISTICS */

    @Override
    public long countReplacementsReviewed(WikipediaLanguage lang) {
        return this.countReviewed.computeIfAbsent(lang, l -> this.replacementStatsDao.countReplacementsReviewed(l));
    }

    @Override
    public long countReplacementsNotReviewed(WikipediaLanguage lang) {
        return this.countNotReviewed.computeIfAbsent(
                lang,
                l -> this.replacementStatsDao.countReplacementsNotReviewed(l)
            );
    }

    @Override
    public List<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang) {
        return this.countByReviewer.computeIfAbsent(
                lang,
                l -> this.replacementStatsDao.countReplacementsGroupedByReviewer(l)
            );
    }

    @Scheduled(fixedDelayString = "${replacer.page.stats.delay}")
    public void scheduledUpdateStatistics() {
        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            this.countReviewed.put(lang, this.replacementStatsDao.countReplacementsReviewed(lang));
            this.countNotReviewed.put(lang, this.replacementStatsDao.countReplacementsNotReviewed(lang));
            this.countByReviewer.put(lang, this.replacementStatsDao.countReplacementsGroupedByReviewer(lang));
        }
    }
}
