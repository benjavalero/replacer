package es.bvalero.replacer.finder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Domain class of a potential replacement in an article.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ArticleReplacement extends MatchResult {
    private String type;
    private String subtype;
    private List<ReplacementSuggestion> suggestions;

    public ArticleReplacement(String text, int start, String type, String subtype, List<ReplacementSuggestion> suggestions) {
        super(start, text);
        this.type = type;
        this.subtype = subtype;
        this.suggestions = suggestions;
    }

    boolean isContainedInListSelfIgnoring(List<ArticleReplacement> articleReplacements) {
        boolean isContained = false;
        for (ArticleReplacement articleReplacement : articleReplacements) {
            if (!this.equals(articleReplacement) && this.isContainedIn(articleReplacement)) {
                isContained = true;
                break;
            }
        }
        return isContained;
    }

}
