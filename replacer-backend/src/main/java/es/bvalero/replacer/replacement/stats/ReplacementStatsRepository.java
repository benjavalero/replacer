package es.bvalero.replacer.replacement.stats;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;

interface ReplacementStatsRepository {
    long countReplacementsReviewed(WikipediaLanguage lang);

    long countReplacementsNotReviewed(WikipediaLanguage lang);

    Collection<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang);
}
