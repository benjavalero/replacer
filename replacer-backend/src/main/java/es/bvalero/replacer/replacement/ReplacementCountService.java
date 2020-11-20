package es.bvalero.replacer.replacement;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReplacementCountService {
    @Autowired
    private ReplacementDao replacementDao;

    // Cache the count of PAGES containing replacements by type/subtype
    // This list is updated every 10 minutes and modified when saving changes
    private Map<WikipediaLanguage, LanguageCount> languageCounts = new EnumMap<>(WikipediaLanguage.class);

    /* STATISTICS */

    long countReplacementsReviewed(WikipediaLanguage lang) {
        return replacementDao.countReplacementsReviewed(lang);
    }

    long countReplacementsNotReviewed(WikipediaLanguage lang) {
        return replacementDao.countReplacementsNotReviewed(lang);
    }

    List<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang) {
        return replacementDao.countReplacementsGroupedByReviewer(lang);
    }

    /* LIST OF REPLACEMENTS */

    List<TypeCount> getCachedReplacementTypeCounts(WikipediaLanguage lang) {
        if (languageCounts.containsKey(lang)) {
            List<TypeCount> list = languageCounts.get(lang).getTypeCounts();
            Collections.sort(list);
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Update the count of misspellings from Wikipedia.
     */
    @VisibleForTesting
    @Scheduled(fixedDelayString = "${replacer.page.stats.delay}")
    public void scheduledUpdateReplacementTypeCounts() {
        LOGGER.info("Scheduled replacement type counts update");
        List<TypeSubtypeCount> counts = replacementDao.countPagesGroupedByTypeAndSubtype();
        this.languageCounts = buildReplacementTypeCounts(counts);
    }

    private Map<WikipediaLanguage, LanguageCount> buildReplacementTypeCounts(List<TypeSubtypeCount> counts) {
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

    public void decreaseCachedReplacementsCount(WikipediaLanguage lang, String type, String subtype, int size) {
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
