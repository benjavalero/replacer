package es.bvalero.replacer.replacement.count;

import static es.bvalero.replacer.repository.ReplacementRepository.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.repository.ReplacementRepository;
import es.bvalero.replacer.repository.TypeSubtypeCount;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Service to retrieve and cache the replacement counts by type.
 * This is the one public which must be used by other services or components.
 */
@Slf4j
@Component
public class ReplacementCountService {

    @Autowired
    private ReplacementRepository replacementRepository;

    // Replacement count cache
    // It's a heavy query in database (several seconds), so we load the counts on start and refresh them periodically.
    // Of course this can lead to a slight misalignment which is fixed in the next refresh.
    // We add synchronization just in case the list is requested while still loading on start.
    private Map<WikipediaLanguage, LanguageCount> replacementCount;

    Collection<TypeCount> countReplacementsGroupedByType(WikipediaLanguage lang) throws ReplacerException {
        return this.getLanguageCount(lang).getTypeCounts();
    }

    @VisibleForTesting
    LanguageCount getLanguageCount(WikipediaLanguage lang) throws ReplacerException {
        return this.getReplacementCount().get(lang);
    }

    public void reviewAsSystemByType(WikipediaLanguage lang, String type, String subtype) {
        this.removeCachedReplacementCount(lang, type, subtype);
        replacementRepository.updateReviewerByType(lang, type, subtype, REVIEWER_SYSTEM);
    }

    @VisibleForTesting
    void removeCachedReplacementCount(WikipediaLanguage lang, String type, String subtype) {
        this.replacementCount.get(lang).removeTypeCount(type, subtype);
    }

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
        this.replacementRepository.updateReviewerByPageAndType(lang, pageId, type, subtype, reviewer);
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
    public void scheduledUpdateReplacementCount() {
        LOGGER.info("Scheduled replacement type counts update");
        this.loadReplacementTypeCounts();
    }

    private synchronized void loadReplacementTypeCounts() {
        Map<WikipediaLanguage, LanguageCount> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            map.put(lang, getReplacementsTypeCountsByLang(lang));
        }
        this.replacementCount = map;
        this.notifyAll();
    }

    private LanguageCount getReplacementsTypeCountsByLang(WikipediaLanguage lang) {
        Collection<TypeSubtypeCount> typeSubtypeCounts = replacementRepository.countReplacementsByType(lang);
        return LanguageCount.build(typeSubtypeCounts);
    }
}
