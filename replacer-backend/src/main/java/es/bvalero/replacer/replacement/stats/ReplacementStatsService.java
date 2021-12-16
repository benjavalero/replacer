package es.bvalero.replacer.replacement.stats;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.ReplacementStatsRepository;
import es.bvalero.replacer.repository.ResultCount;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service to retrieve and cache the replacement counts for statistics */
@Service
class ReplacementStatsService {

    @Autowired
    private ReplacementStatsRepository replacementStatsRepository;

    long countReplacementsReviewed(WikipediaLanguage lang) {
        return this.replacementStatsRepository.countReplacementsReviewed(lang);
    }

    long countReplacementsNotReviewed(WikipediaLanguage lang) {
        return this.replacementStatsRepository.countReplacementsNotReviewed(lang);
    }

    Collection<ResultCount<String>> countReplacementsGroupedByReviewer(WikipediaLanguage lang) {
        return this.replacementStatsRepository.countReplacementsByReviewer(lang);
    }
}
