package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import lombok.Value;

@Value
public class PageReviewOptions {
    WikipediaLanguage lang;
    String type;
    String subtype;
    String suggestion;

    WikipediaLanguage getLang() {
        return lang == null ? WikipediaLanguage.SPANISH : lang;
    }

    static PageReviewOptions ofNoType(WikipediaLanguage lang) {
        return new PageReviewOptions(lang, null, null, null);
    }

    static PageReviewOptions ofTypeSubtype(WikipediaLanguage lang, String type, String subtype) {
        return new PageReviewOptions(lang, type, subtype, null);
    }

    static PageReviewOptions ofCustom(WikipediaLanguage lang, String replacement, String suggestion) {
        return new PageReviewOptions(lang, ReplacementFindService.CUSTOM_FINDER_TYPE, replacement, suggestion);
    }
}
