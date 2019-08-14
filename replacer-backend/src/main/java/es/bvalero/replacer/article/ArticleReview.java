package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.ArticleReplacement;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Domain class of an article to review to be used in the front-end.
 */
@Value(staticConstructor = "of")
@Builder
class ArticleReview {

    private Integer articleId;
    private String title;
    private String content;
    private String currentTimestamp;
    private List<ArticleReplacement> replacements;

}
