package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.IndexedPage;
import java.util.Collection;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;

/** Service to retrieve the replacement counts for statistics */
@Service
class ReplacementCountService {

    @VisibleForTesting
    static final int NUM_RESULTS = 20;

    // Dependency injection
    private final ReplacementCountRepository replacementCountRepository;

    ReplacementCountService(ReplacementCountRepository replacementCountRepository) {
        this.replacementCountRepository = replacementCountRepository;
    }

    /** Count the number of reviewed replacements including the custom ones */
    int countReviewed(WikipediaLanguage lang) {
        return replacementCountRepository.countReviewed(lang);
    }

    /** Count the number of replacements to review */
    int countNotReviewed(WikipediaLanguage lang) {
        return replacementCountRepository.countNotReviewed(lang);
    }

    /**
     * Count the number of reviewed replacements, including the custom ones,
     * grouped by reviewer in descending order by count.
     */
    Collection<ResultCount<String>> countReviewedGroupedByReviewer(WikipediaLanguage lang) {
        return replacementCountRepository.countReviewedGroupedByReviewer(lang).stream().sorted().toList();
    }

    /** Count the number of replacements to review grouped by page in descending order by count */
    Collection<ResultCount<IndexedPage>> countNotReviewedGroupedByPage(WikipediaLanguage lang) {
        // For the moment we are not going to cache it as it is used only by admins
        return replacementCountRepository.countNotReviewedGroupedByPage(lang, NUM_RESULTS).stream().sorted().toList();
    }
}
