package es.bvalero.replacer.article;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

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
    @Wither
    private Integer section;
    private String queryTimestamp;
    @Wither
    private List<ArticleReplacement> replacements;
}
