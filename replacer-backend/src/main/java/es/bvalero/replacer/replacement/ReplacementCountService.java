package es.bvalero.replacer.replacement;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReplacementCountService {
    @Autowired
    private ReplacementRepository replacementRepository;

    // Cache the count of PAGES with replacements
    // This list is updated every 10 minutes and modified when saving changes
    private Map<WikipediaLanguage, LanguageCount> languageCounts = new EnumMap<>(WikipediaLanguage.class);

    /* STATISTICS */

    long countReplacementsReviewed(WikipediaLanguage lang) {
        return replacementRepository.countByLangAndReviewerIsNotNullAndReviewerIsNot(
            lang.getCode(),
            ReplacementIndexService.SYSTEM_REVIEWER
        );
    }

    long countReplacementsToReview(WikipediaLanguage lang) {
        return replacementRepository.countByLangAndReviewerIsNull(lang.getCode());
    }

    List<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang) {
        return replacementRepository.countGroupedByReviewer(lang.getCode(), ReplacementIndexService.SYSTEM_REVIEWER);
    }

    /* LIST OF REPLACEMENTS */

    List<TypeCount> findReplacementCount(WikipediaLanguage lang) {
        if (languageCounts.containsKey(lang)) {
            return languageCounts.get(lang).getTypeCounts();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Update the count of misspellings from Wikipedia.
     */
    @Scheduled(fixedDelayString = "${replacer.page.stats.delay}")
    void updateReplacementCount() {
        LOGGER.info("EXECUTE Scheduled update of grouped replacements count");
        LOGGER.info("START Count grouped replacements by type and subtype");
        List<TypeSubtypeCount> counts = replacementRepository.countGroupedByTypeAndSubtype();
        LOGGER.info("END Count grouped replacements. Size: {}", counts.size());
        this.languageCounts = loadCachedReplacementCount(counts);
    }

    private Map<WikipediaLanguage, LanguageCount> loadCachedReplacementCount(List<TypeSubtypeCount> counts) {
        final Map<WikipediaLanguage, LanguageCount> langCounts = new EnumMap<>(WikipediaLanguage.class);
        for (TypeSubtypeCount count : counts) {
            WikipediaLanguage lang = WikipediaLanguage.forValues(count.getLang());
            if (!langCounts.containsKey(lang)) {
                langCounts.put(lang, new LanguageCount(lang));
            }
            LanguageCount languageCount = langCounts.get(lang);

            String type = count.getType();
            if (!languageCount.contains(type)) {
                languageCount.add(new TypeCount(type));
            }
            TypeCount typeCount = languageCount.get(type);

            typeCount.add(new SubtypeCount(count.getSubtype(), count.getCount()));
        }
        return langCounts;
    }

    public void removeCachedReplacementCount(WikipediaLanguage lang, String type, String subtype) {
        if (languageCounts.containsKey(lang)) {
            LanguageCount languageCount = languageCounts.get(lang);
            if (languageCount.contains(type)) {
                TypeCount typeCount = languageCount.get(type);
                typeCount.remove(subtype);

                // Empty parent if children are empty
                if (typeCount.isEmpty()) {
                    languageCount.remove(type);
                }
            }
        }
    }

    void decreaseCachedReplacementsCount(WikipediaLanguage lang, String type, String subtype, int size) {
        if (languageCounts.containsKey(lang)) {
            LanguageCount languageCount = languageCounts.get(lang);
            if (languageCount.contains(type)) {
                Optional<SubtypeCount> currentCount = languageCount.get(type).get(subtype);
                if (currentCount.isPresent()) {
                    long newCount = currentCount.get().getCount() - size;
                    if (newCount > 0) {
                        currentCount.get().setCount(newCount);
                    } else {
                        // Clean the possible empty counts after decreasing
                        removeCachedReplacementCount(lang, type, subtype);
                    }
                }
            }
        }
    }
}
