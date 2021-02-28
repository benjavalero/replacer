package es.bvalero.replacer.replacement;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
class ReplacementDaoProxy implements ReplacementStatsDao {

    @Autowired
    private ReplacementStatsDao replacementStatsDao;

    // Replacement count cache
    // It's a heavy query in database so we preload the counts on start and refresh them periodically.
    // We add synchronization just in case the list is requested while still loading on start.
    private Map<WikipediaLanguage, LanguageCount> replacementCount;

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

    @Override
    public LanguageCount countReplacementsGroupedByType(WikipediaLanguage lang) throws ReplacerException {
        return this.getReplacementCount().get(lang);
    }

    @Override
    public void reviewByPageId(
        WikipediaLanguage lang,
        int pageId,
        @Nullable String type,
        @Nullable String subtype,
        String reviewer
    ) {
        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(subtype)) {
            this.decrementSubtypeCount(lang, type, subtype);
        }
        this.replacementStatsDao.reviewByPageId(lang, pageId, type, subtype, reviewer);
    }

    @Override
    public void reviewAsSystemBySubtype(WikipediaLanguage lang, String type, String subtype) {
        this.removeCachedReplacementCount(lang, type, subtype);
        this.replacementStatsDao.reviewAsSystemBySubtype(lang, type, subtype);
    }

    /* SCHEDULED UPDATE OF CACHE */

    private synchronized Map<WikipediaLanguage, LanguageCount> getReplacementCount() throws ReplacerException {
        while (this.replacementCount == null) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ReplacerException(e);
            }
        }
        return this.replacementCount;
    }

    @Scheduled(fixedDelayString = "${replacer.page.stats.delay}")
    public void scheduledUpdateReplacementCount() throws ReplacerException {
        LOGGER.info("Scheduled replacement type counts update");
        this.loadReplacementTypeCounts();
    }

    @Loggable(value = Loggable.TRACE)
    private synchronized void loadReplacementTypeCounts() throws ReplacerException {
        Map<WikipediaLanguage, LanguageCount> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            map.put(lang, replacementStatsDao.countReplacementsGroupedByType(lang));
        }
        this.replacementCount = map;
        this.notifyAll();
    }

    @Scheduled(fixedDelayString = "${replacer.page.stats.delay}")
    public void scheduledUpdateStatistics() {
        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            this.countReviewed.put(lang, this.replacementStatsDao.countReplacementsReviewed(lang));
            this.countNotReviewed.put(lang, this.replacementStatsDao.countReplacementsNotReviewed(lang));
            this.countByReviewer.put(lang, this.replacementStatsDao.countReplacementsGroupedByReviewer(lang));
        }
    }

    @VisibleForTesting
    void removeCachedReplacementCount(WikipediaLanguage lang, String type, String subtype) {
        this.replacementCount.get(lang).removeTypeCount(type, subtype);
    }

    @VisibleForTesting
    void decrementSubtypeCount(WikipediaLanguage lang, String type, String subtype) {
        this.replacementCount.get(lang).decrementSubtypeCount(type, subtype);
    }
}
