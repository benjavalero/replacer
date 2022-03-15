package es.bvalero.replacer.replacement.count;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.PageModel;
import es.bvalero.replacer.repository.ReplacementCountRepository;
import es.bvalero.replacer.repository.ResultCount;
import java.util.Collection;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service to retrieve and cache the replacement counts for statistics */
@Service
class ReplacementCountService {

    @VisibleForTesting
    static final int NUM_RESULTS = 20;

    @Autowired
    private ReplacementCountRepository replacementCountRepository;

    int countReplacementsReviewed(WikipediaLanguage lang) {
        return this.replacementCountRepository.countReplacementsReviewed(lang);
    }

    int countReplacementsNotReviewed(WikipediaLanguage lang) {
        return this.replacementCountRepository.countReplacementsNotReviewed(lang);
    }

    Collection<ResultCount<String>> countReplacementsGroupedByReviewer(WikipediaLanguage lang) {
        return this.replacementCountRepository.countReplacementsByReviewer(lang);
    }

    Collection<ResultCount<PageModel>> countReplacementsGroupedByPage(WikipediaLanguage lang) {
        return replacementCountRepository.countReplacementsByPage(lang, NUM_RESULTS);
    }
}
