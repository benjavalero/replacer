package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.List;

interface ReplacementStatsDao {
    long countReplacementsReviewed(WikipediaLanguage lang);

    long countReplacementsNotReviewed(WikipediaLanguage lang);

    List<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang);
}
