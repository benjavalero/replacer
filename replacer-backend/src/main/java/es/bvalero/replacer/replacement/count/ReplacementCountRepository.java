package es.bvalero.replacer.replacement.count;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import org.springframework.lang.Nullable;

interface ReplacementCountRepository {
    LanguageCount countReplacementsGroupedByType(WikipediaLanguage lang) throws ReplacerException;

    void reviewAsSystemByType(WikipediaLanguage lang, String type, String subtype);

    void reviewByPageId(
        WikipediaLanguage lang,
        int pageId,
        @Nullable String type,
        @Nullable String subtype,
        String reviewer
    );
}
