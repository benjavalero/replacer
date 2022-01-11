package es.bvalero.replacer.replacement.count.cache;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import es.bvalero.replacer.repository.ResultCount;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Implementation of the replacement repository which maintains a cache of the replacement counts */
@Slf4j
@Primary
@Transactional
@Repository
class ReplacementTypeCacheRepository implements ReplacementTypeRepository {

    @Autowired
    @Qualifier("replacementJdbcRepository")
    private ReplacementTypeRepository replacementTypeRepository;

    // Replacement count cache
    // It's a heavy query in database (several seconds), so we load the counts on start and refresh them periodically.
    // This may lead to a slight misalignment, which is fixed in the next refresh,
    // as modifications in the cache may happen while the count query is in progress.
    // We add synchronization just in case the list is requested while still loading on start.
    private Map<WikipediaLanguage, KindCount> replacementCount;

    @Override
    public Collection<ResultCount<ReplacementType>> countReplacementsByType(WikipediaLanguage lang) {
        try {
            return this.getKindCount(lang).toModel();
        } catch (ReplacerException e) {
            return Collections.emptyList();
        }
    }

    @VisibleForTesting
    KindCount getKindCount(WikipediaLanguage lang) throws ReplacerException {
        return this.getReplacementCount().get(lang);
    }

    @Override
    public void updateReviewerByType(WikipediaLanguage lang, ReplacementType type, String reviewer) {
        this.removeCachedReplacementCount(lang, type);
        replacementTypeRepository.updateReviewerByType(lang, type, reviewer);
    }

    @VisibleForTesting
    void removeCachedReplacementCount(WikipediaLanguage lang, ReplacementType type) {
        this.replacementCount.get(lang).removeTypeCount(type.getKind(), type.getSubtype());
    }

    @Override
    public void updateReviewerByPageAndType(WikipediaPageId pageId, @Nullable ReplacementType type, String reviewer) {
        if (Objects.nonNull(type)) {
            this.decrementSubtypeCount(pageId.getLang(), type);
        }
        this.replacementTypeRepository.updateReviewerByPageAndType(pageId, type, reviewer);
    }

    @VisibleForTesting
    void decrementSubtypeCount(WikipediaLanguage lang, ReplacementType type) {
        this.replacementCount.get(lang).decrementSubtypeCount(type.getKind(), type.getSubtype());
    }

    @Override
    public void removeReplacementsByType(WikipediaLanguage lang, Collection<ReplacementType> types) {
        types.forEach(type -> removeCachedReplacementCount(lang, type));
        this.replacementTypeRepository.removeReplacementsByType(lang, types);
    }

    /* SCHEDULED UPDATE OF CACHE */

    private synchronized Map<WikipediaLanguage, KindCount> getReplacementCount() throws ReplacerException {
        while (this.replacementCount == null) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Error getting the synchronized replacement count");
                throw new ReplacerException(e);
            }
        }
        return this.replacementCount;
    }

    @Loggable(value = LogLevel.DEBUG, skipArgs = true, skipResult = true)
    @Scheduled(fixedDelayString = "${replacer.page.stats.delay}")
    public void scheduledUpdateReplacementCount() {
        this.loadReplacementTypeCounts();
    }

    private synchronized void loadReplacementTypeCounts() {
        Map<WikipediaLanguage, KindCount> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            map.put(lang, getReplacementsTypeCountsByLang(lang));
        }
        this.replacementCount = map;
        this.notifyAll();
    }

    private KindCount getReplacementsTypeCountsByLang(WikipediaLanguage lang) {
        return KindCount.fromModel(this.replacementTypeRepository.countReplacementsByType(lang));
    }
}
