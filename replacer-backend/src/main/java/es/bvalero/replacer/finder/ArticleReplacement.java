package es.bvalero.replacer.finder;

import es.bvalero.replacer.persistence.ReplacementType;
import org.jetbrains.annotations.NonNls;

/**
 * Domain class of a potential replacement in an article.
 */
public class ArticleReplacement extends MatchResult {
    private ReplacementType type;
    private String subtype;
    private String comment;
    private String suggestion;

    public ArticleReplacement(String text, int start, ReplacementType type, String subtype, String comment, String suggestion) {
        super(start, text);
        this.type = type;
        this.subtype = subtype;
        this.comment = comment;
        this.suggestion = suggestion;
    }

    public ReplacementType getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    @SuppressWarnings("unused")
    public String getComment() {
        return comment;
    }

    public String getSuggestion() {
        return suggestion;
    }

    @NonNls
    @Override
    public String toString() {
        return "ArticleReplacement{" +
                "text='" + getText() + '\'' +
                ", start=" + getStart() +
                ", type=" + type +
                ", subtype='" + subtype + '\'' +
                ", comment='" + comment + '\'' +
                ", suggestion='" + suggestion + '\'' +
                '}';
    }

}
