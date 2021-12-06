package es.bvalero.replacer.replacement.stats;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class ReplacementStatsService {

    @Autowired
    private ReplacementStatsRepository replacementStatsRepository;

    long countReplacementsReviewed(WikipediaLanguage lang) {
        return replacementStatsRepository.countReplacementsReviewed(lang);
    }

    public long countReplacementsNotReviewed(WikipediaLanguage lang) {
        return replacementStatsRepository.countReplacementsNotReviewed(lang);
    }

    Collection<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang) {
        return replacementStatsRepository.countReplacementsGroupedByReviewer(lang);
    }
}
