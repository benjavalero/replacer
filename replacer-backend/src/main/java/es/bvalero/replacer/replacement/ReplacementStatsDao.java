package es.bvalero.replacer.replacement;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.List;

interface ReplacementStatsDao {
    long countReplacementsReviewed(WikipediaLanguage lang);

    long countReplacementsNotReviewed(WikipediaLanguage lang);

    List<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang);

    LanguageCount countReplacementsGroupedByType(WikipediaLanguage lang) throws ReplacerException;
}
