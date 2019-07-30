package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.ArticleReplacement;
import lombok.Value;

import java.util.List;

/**
 * Domain class of an article to review to be used in the front-end.
 */
@Value
class ArticleReview {

    private Integer articleId;
    private String title;
    private String content;
    private List<ArticleReplacement> replacements;
    private String currentTimestamp;

}
