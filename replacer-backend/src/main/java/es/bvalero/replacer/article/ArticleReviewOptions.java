package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.ReplacementFindService;
import lombok.Value;

@Value
public class ArticleReviewOptions {
    String type;
    String subtype;
    String suggestion;

    static ArticleReviewOptions ofNoType() {
        return new ArticleReviewOptions(null, null, null);
    }

    static ArticleReviewOptions ofTypeSubtype(String type, String subtype) {
        return new ArticleReviewOptions(type, subtype, null);
    }

    static ArticleReviewOptions ofCustom(String replacement, String suggestion) {
        return new ArticleReviewOptions(ReplacementFindService.CUSTOM_FINDER_TYPE, replacement, suggestion);
    }
}
