package es.bvalero.replacer.replacement;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.ReplacerException;
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
    private Map<WikipediaLanguage, LanguageCount> languageCounts;

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

    List<TypeCount> getCachedReplacementTypeCounts(WikipediaLanguage lang) throws ReplacerException {
        return this.getLanguageCounts().getOrDefault(lang, LanguageCount.ofEmpty()).getTypeCounts();
    }

    private synchronized Map<WikipediaLanguage, LanguageCount> getLanguageCounts() throws ReplacerException {
        while (this.languageCounts == null) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ReplacerException(e);
            }
        }
        return this.languageCounts;
    }

    /**
     * Update the count of misspellings from Wikipedia.
     */
    @VisibleForTesting
    @Scheduled(fixedDelayString = "${replacer.page.stats.delay}")
    public void scheduledUpdateReplacementTypeCounts() {
        LOGGER.info("Scheduled replacement type counts update");
        this.loadReplacementTypeCounts();
    }

    @Loggable(value = Loggable.TRACE)
    private synchronized void loadReplacementTypeCounts() {
        List<TypeSubtypeCount> counts = replacementDao.countPagesGroupedByTypeAndSubtype();
        this.languageCounts = buildReplacementTypeCounts(counts);
        this.notifyAll();
    }

    private Map<WikipediaLanguage, LanguageCount> buildReplacementTypeCounts(List<TypeSubtypeCount> counts) {
        final Map<WikipediaLanguage, LanguageCount> langCounts = new EnumMap<>(WikipediaLanguage.class);
        for (TypeSubtypeCount count : counts) {
            WikipediaLanguage lang = count.getLang();
            LanguageCount languageCount = langCounts.computeIfAbsent(lang, LanguageCount::new);

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
                TypeCount typeCount = languageCount.get(type);
                typeCount
                    .get(subtype)
                    .ifPresent(
                        subtypeCount -> {
                            long newCount = subtypeCount.getCount() - size;
                            if (newCount > 0) {
                                // Update the subtype with the new count
                                typeCount.add(subtypeCount.withCount(newCount));
                            } else {
                                // Remove the subtype count as in method "removeCachedReplacementCount"
                                typeCount.remove(subtype);

                                // Empty parent if children are empty
                                if (typeCount.isEmpty()) {
                                    languageCount.remove(type);
                                }
                            }
                        }
                    );
            }
        }
    }
}
