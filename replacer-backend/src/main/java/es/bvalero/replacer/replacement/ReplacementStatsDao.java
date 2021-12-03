package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import java.util.List;
import org.springframework.lang.Nullable;

interface ReplacementStatsDao {
    long countReplacementsReviewed(WikipediaLanguage lang);

    long countReplacementsNotReviewed(WikipediaLanguage lang);

    List<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang);

    LanguageCount countReplacementsGroupedByType(WikipediaLanguage lang) throws ReplacerException;

    void reviewByPageId(
        WikipediaLanguage lang,
        int pageId,
        @Nullable String type,
        @Nullable String subtype,
        String reviewer
    );
}
