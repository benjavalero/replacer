package es.bvalero.replacer.repository;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;

public interface ReplacementStatsRepository {
    /** Count the number of replacements reviewed */
    long countReplacementsReviewed(WikipediaLanguage lang);

    /** Count the number of replacements to review */
    long countReplacementsNotReviewed(WikipediaLanguage lang);

    /** Count the number of reviewed replacements by reviewer */
    Collection<ResultCount<String>> countReplacementsByReviewer(WikipediaLanguage lang);
}