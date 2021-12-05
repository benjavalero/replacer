package es.bvalero.replacer.replacement.count;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import org.springframework.lang.Nullable;

interface ReplacementCountRepository {
    Collection<TypeSubtypeCount> countReplacementTypesByLang(WikipediaLanguage lang);

    void reviewAsSystemByType(WikipediaLanguage lang, String type, String subtype);

    void reviewByPageId(
        WikipediaLanguage lang,
        int pageId,
        @Nullable String type,
        @Nullable String subtype,
        String reviewer
    );
}
