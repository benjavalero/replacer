package es.bvalero.replacer.finder;

import org.jetbrains.annotations.NonNls;

import java.util.List;
import java.util.Objects;

/**
 * Domain class of a potential replacement in an article.
 */
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

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public List<ReplacementSuggestion> getSuggestions() {
        return suggestions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ArticleReplacement that = (ArticleReplacement) o;
        return type.equals(that.type) &&
                subtype.equals(that.subtype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, subtype);
    }

    @NonNls
    @Override
    public String toString() {
        return "ArticleReplacement{" +
                "text='" + getText() + '\'' +
                ", start=" + getStart() +
                ", type=" + type +
                ", subtype='" + subtype + '\'' +
                ", suggestions='" + suggestions + '\'' +
                '}';
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
