package es.bvalero.replacer.article;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Domain class of an article to review to be used in the front-end.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class ArticleReview {
    private int id;
    private String title;
    private String content;
    private Integer section;
    private String queryTimestamp;
    private List<ArticleReplacement> replacements;
}
