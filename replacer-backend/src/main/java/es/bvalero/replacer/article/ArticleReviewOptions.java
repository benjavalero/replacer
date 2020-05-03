package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import lombok.Value;

@Value
public class ArticleReviewOptions {
    WikipediaLanguage lang;
    String type;
    String subtype;
    String suggestion;

    WikipediaLanguage getLang() {
        return lang == null ? WikipediaLanguage.SPANISH : lang;
    }

    static ArticleReviewOptions ofNoType(WikipediaLanguage lang) {
        return new ArticleReviewOptions(lang, null, null, null);
    }

    static ArticleReviewOptions ofTypeSubtype(WikipediaLanguage lang, String type, String subtype) {
        return new ArticleReviewOptions(lang, type, subtype, null);
    }

    static ArticleReviewOptions ofCustom(WikipediaLanguage lang, String replacement, String suggestion) {
        return new ArticleReviewOptions(lang, ReplacementFindService.CUSTOM_FINDER_TYPE, replacement, suggestion);
    }
}
