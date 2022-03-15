package es.bvalero.replacer.replacement.stats;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.PageModel;
import es.bvalero.replacer.repository.ReplacementStatsRepository;
import es.bvalero.replacer.repository.ResultCount;
import java.util.Collection;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service to retrieve and cache the replacement counts for statistics */
@Service
class ReplacementStatsService {

    @VisibleForTesting
    static final int NUM_RESULTS = 20;

    @Autowired
    private ReplacementStatsRepository replacementStatsRepository;

    int countReplacementsReviewed(WikipediaLanguage lang) {
        return this.replacementStatsRepository.countReplacementsReviewed(lang);
    }

    int countReplacementsNotReviewed(WikipediaLanguage lang) {
        return this.replacementStatsRepository.countReplacementsNotReviewed(lang);
    }

    Collection<ResultCount<String>> countReplacementsGroupedByReviewer(WikipediaLanguage lang) {
        return this.replacementStatsRepository.countReplacementsByReviewer(lang);
    }

    Collection<ResultCount<PageModel>> countReplacementsGroupedByPage(WikipediaLanguage lang) {
        return replacementStatsRepository.countReplacementsByPage(lang, NUM_RESULTS);
    }
}
