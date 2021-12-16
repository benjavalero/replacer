package es.bvalero.replacer.replacement.count;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.repository.ReplacementModel;
import es.bvalero.replacer.repository.ReplacementRepository;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import es.bvalero.replacer.repository.ResultCount;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
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

/**
 * Implementation of the replacement repository which maintains a cache of the replacement counts.
 * This is the one public which must be used by other services or components.
 */
@Slf4j
@Primary
@Transactional
@Repository
class ReplacementCacheRepository implements ReplacementRepository, ReplacementTypeRepository {

    @Autowired
    @Qualifier("replacementJdbcRepository")
    private ReplacementRepository replacementRepository;

    @Autowired
    @Qualifier("replacementJdbcRepository")
    private ReplacementTypeRepository replacementTypeRepository;

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

    @Override
    public void updateReviewerByType(WikipediaLanguage lang, ReplacementType type, String reviewer) {
        this.removeCachedReplacementCount(lang, type);
        replacementTypeRepository.updateReviewerByType(lang, type, reviewer);
    }

    @VisibleForTesting
    void removeCachedReplacementCount(WikipediaLanguage lang, ReplacementType type) {
        this.replacementCount.get(lang).removeTypeCount(type.getKind().getLabel(), type.getSubtype());
    }

    @Override
    public void updateReviewerByPageAndType(
        WikipediaLanguage lang,
        int pageId,
        @Nullable ReplacementType type,
        String reviewer
    ) {
        if (Objects.nonNull(type)) {
            this.decrementSubtypeCount(lang, type);
        }
        this.replacementTypeRepository.updateReviewerByPageAndType(lang, pageId, type, reviewer);
    }

    @VisibleForTesting
    void decrementSubtypeCount(WikipediaLanguage lang, ReplacementType type) {
        this.replacementCount.get(lang).decrementSubtypeCount(type.getKind().getLabel(), type.getSubtype());
    }

    @Override
    public void removeReplacementsByType(WikipediaLanguage lang, Collection<ReplacementType> types) {
        types.forEach(type -> removeCachedReplacementCount(lang, type));
        this.replacementTypeRepository.removeReplacementsByType(lang, types);
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

    @Loggable(value = LogLevel.DEBUG, skipArgs = true, skipResult = true)
    @Scheduled(fixedDelayString = "${replacer.page.stats.delay}")
    public void scheduledUpdateReplacementCount() {
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
        Collection<ResultCount<ReplacementType>> typeSubtypeCounts = countReplacementsByType(lang);
        return LanguageCount.build(typeSubtypeCounts);
    }

    /* NOT OVERWRITTEN */

    @Override
    public void addReplacements(Collection<ReplacementModel> replacements) {
        replacementRepository.addReplacements(replacements);
    }

    @Override
    public void updateReplacements(Collection<ReplacementModel> replacements) {
        replacementRepository.updateReplacements(replacements);
    }

    @Override
    public void removeReplacements(Collection<ReplacementModel> replacements) {
        replacementRepository.removeReplacements(replacements);
    }

    @Override
    public Collection<ResultCount<ReplacementType>> countReplacementsByType(WikipediaLanguage lang) {
        return replacementTypeRepository.countReplacementsByType(lang);
    }

    @Override
    public long findReplacementToReview(WikipediaLanguage lang, long chunkSize) {
        return replacementRepository.findReplacementToReview(lang, chunkSize);
    }
}
