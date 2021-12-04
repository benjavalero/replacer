package es.bvalero.replacer.replacement.count;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import java.util.EnumMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Implementation of the replacement repository which maintains a cache of the replacement counts */
@Slf4j
@Primary
@Component
@Qualifier("replacementCountCacheRepository")
class ReplacementCountCacheRepository implements ReplacementCountRepository {

    @Autowired
    @Qualifier("replacementJdbcRepository")
    private ReplacementCountRepository replacementCountRepository;

    // Replacement count cache
    // It's a heavy query in database (several seconds), so we load the counts on start and refresh them periodically.
    // Of course this can lead to a slight misalignment which is fixed in the next refresh.
    // We add synchronization just in case the list is requested while still loading on start.
    private Map<WikipediaLanguage, LanguageCount> replacementCount;

    @Override
    public LanguageCount countReplacementsGroupedByType(WikipediaLanguage lang) throws ReplacerException {
        return this.getReplacementCount().get(lang);
    }

    @Override
    public void reviewAsSystemByType(WikipediaLanguage lang, String type, String subtype) {
        this.removeCachedReplacementCount(lang, type, subtype);
        replacementCountRepository.reviewAsSystemByType(lang, type, subtype);
    }

    @VisibleForTesting
    void removeCachedReplacementCount(WikipediaLanguage lang, String type, String subtype) {
        this.replacementCount.get(lang).removeTypeCount(type, subtype);
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
        this.replacementCountRepository.reviewByPageId(lang, pageId, type, subtype, reviewer);
    }

    @VisibleForTesting
    void decrementSubtypeCount(WikipediaLanguage lang, String type, String subtype) {
        this.replacementCount.get(lang).decrementSubtypeCount(type, subtype);
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
            map.put(lang, replacementCountRepository.countReplacementsGroupedByType(lang));
        }
        this.replacementCount = map;
        this.notifyAll();
    }
}
