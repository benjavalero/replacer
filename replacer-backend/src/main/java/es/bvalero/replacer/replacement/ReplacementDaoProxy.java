package es.bvalero.replacer.replacement;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Qualifier("replacementProxy")
@Component
class ReplacementDaoProxy implements ReplacementDao {

    @Autowired
    @Qualifier("replacementDao")
    private ReplacementDao replacementDao;

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
        return this.countReviewed.computeIfAbsent(lang, l -> this.replacementDao.countReplacementsReviewed(l));
    }

    @Override
    public long countReplacementsNotReviewed(WikipediaLanguage lang) {
        return this.countNotReviewed.computeIfAbsent(lang, l -> this.replacementDao.countReplacementsNotReviewed(l));
    }

    @Override
    public List<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang) {
        return this.countByReviewer.computeIfAbsent(
                lang,
                l -> this.replacementDao.countReplacementsGroupedByReviewer(l)
            );
    }

    @Override
    public List<String> findPageTitlesToReviewBySubtype(WikipediaLanguage lang, String type, String subtype) {
        return this.replacementDao.findPageTitlesToReviewBySubtype(lang, type, subtype);
    }

    @Override
    public LanguageCount countReplacementsGroupedByType(WikipediaLanguage lang) throws ReplacerException {
        return this.getReplacementCount().get(lang);
    }

    @Override
    public void insert(ReplacementEntity entity) {
        this.replacementDao.insert(entity);
    }

    @Override
    public void insert(List<ReplacementEntity> entityList) {
        this.replacementDao.insert(entityList);
    }

    @Override
    public void update(ReplacementEntity entity) {
        this.replacementDao.update(entity);
    }

    @Override
    public void update(List<ReplacementEntity> entityList) {
        this.replacementDao.update(entityList);
    }

    @Override
    public void updateDate(List<ReplacementEntity> entityList) {
        this.replacementDao.updateDate(entityList);
    }

    @Override
    public void deleteAll(List<ReplacementEntity> entityList) {
        this.replacementDao.deleteAll(entityList);
    }

    @Override
    public List<ReplacementEntity> findByPageInterval(int minPageId, int maxPageId, WikipediaLanguage lang) {
        return this.replacementDao.findByPageInterval(minPageId, maxPageId, lang);
    }

    @Override
    public void deleteObsoleteByPageId(WikipediaLanguage lang, Set<Integer> pageIds) {
        this.replacementDao.deleteObsoleteByPageId(lang, pageIds);
    }

    @Override
    public List<ReplacementEntity> findByPageId(int pageId, WikipediaLanguage lang) {
        return this.replacementDao.findByPageId(pageId, lang);
    }

    @Override
    public long findRandomIdToBeReviewed(long chunkSize, WikipediaLanguage lang) {
        return this.replacementDao.findRandomIdToBeReviewed(chunkSize, lang);
    }

    @Override
    public List<Integer> findPageIdsToBeReviewed(WikipediaLanguage lang, long start, Pageable pageable) {
        return this.replacementDao.findPageIdsToBeReviewed(lang, start, pageable);
    }

    @Override
    public List<Integer> findRandomPageIdsToBeReviewedBySubtype(
        WikipediaLanguage lang,
        String type,
        String subtype,
        Pageable pageable
    ) {
        List<Integer> pageIds =
            this.replacementDao.findRandomPageIdsToBeReviewedBySubtype(lang, type, subtype, pageable);
        if (pageIds.isEmpty()) {
            // If there are no results empty the cached count for the replacement
            removeCachedReplacementCount(lang, type, subtype);
        }
        return pageIds;
    }

    @Override
    public long countPagesToBeReviewedBySubtype(WikipediaLanguage lang, String type, String subtype) {
        return this.replacementDao.countPagesToBeReviewedBySubtype(lang, type, subtype);
    }

    @Override
    public List<Integer> findPageIdsReviewedByCustomTypeAndSubtype(WikipediaLanguage lang, String subtype) {
        return this.replacementDao.findPageIdsReviewedByCustomTypeAndSubtype(lang, subtype);
    }

    @Override
    public void reviewByPageId(
        WikipediaLanguage lang,
        int pageId,
        @Nullable String type,
        @Nullable String subtype,
        String reviewer
    ) {
        this.replacementDao.reviewByPageId(lang, pageId, type, subtype, reviewer);

        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(subtype)) {
            // Decrement the cached count (one page)
            decrementSubtypeCount(lang, type, subtype);
        }
    }

    @Override
    public void reviewAsSystemBySubtype(WikipediaLanguage lang, String type, String subtype) {
        this.replacementDao.reviewAsSystemBySubtype(lang, type, subtype);

        // Remove from the replacement count cache
        removeCachedReplacementCount(lang, type, subtype);
    }

    @Override
    public void deleteToBeReviewedBySubtype(WikipediaLanguage lang, String type, Set<String> subtypes) {
        this.replacementDao.deleteToBeReviewedBySubtype(lang, type, subtypes);
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
            map.put(lang, replacementDao.countReplacementsGroupedByType(lang));
        }
        this.replacementCount = map;
        this.notifyAll();
    }

    @Scheduled(fixedDelayString = "${replacer.page.stats.delay}")
    public void scheduledUpdateStatistics() {
        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            this.countReviewed.put(lang, this.replacementDao.countReplacementsReviewed(lang));
            this.countNotReviewed.put(lang, this.replacementDao.countReplacementsNotReviewed(lang));
            this.countByReviewer.put(lang, this.replacementDao.countReplacementsGroupedByReviewer(lang));
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
