package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import java.util.List;

interface ReplacementStatsDao {
    long countReplacementsReviewed(WikipediaLanguage lang);

    long countReplacementsNotReviewed(WikipediaLanguage lang);

    List<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang);

    LanguageCount countReplacementsGroupedByType(WikipediaLanguage lang) throws ReplacerException;

    void reviewAsSystemBySubtype(WikipediaLanguage lang, String type, String subtype);
}
