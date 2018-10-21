package es.bvalero.replacer.article;

import es.bvalero.replacer.persistence.ReplacementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Domain class of a potential replacement in an article.
 */
@SuppressWarnings("unused")
public final class ArticleReplacement implements Comparable<ArticleReplacement> {

    private final String text;
    private final int start;
    private final ReplacementType type;
    private final String subtype;
    private final String comment;
    private final String suggestion;

    private ArticleReplacement(String text, int start, ReplacementType type, String subtype, String comment, String suggestion) {
        this.text = text;
        this.start = start;
        this.type = type;
        this.subtype = subtype;
        this.comment = comment;
        this.suggestion = suggestion;
    }

    public static ArticleReplacement.ArticleReplacementBuilder builder() {
        return new ArticleReplacement.ArticleReplacementBuilder();
    }

    public String getText() {
        return text;
    }

    public int getStart() {
        return start;
    }

    public ReplacementType getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getComment() {
        return comment;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public int getEnd() {
        return start + text.length();
    }

    public ArticleReplacement withText(String text) {
        if (this.text.equals(text)) return this;
        return new ArticleReplacement(text, start, type, subtype, comment, suggestion);
    }

    public ArticleReplacement withStart(int start) {
        if (this.start == start) return this;
        return new ArticleReplacement(text, start, type, subtype, comment, suggestion);
    }

    public ArticleReplacement withSubtype(String subtype) {
        if (this.subtype == null ? subtype == null : this.subtype.equals(subtype)) return this;
        return new ArticleReplacement(text, start, type, subtype, comment, suggestion);
    }

    public ArticleReplacement withComment(String comment) {
        if (this.comment == null ? comment == null : this.comment.equals(comment)) return this;
        return new ArticleReplacement(text, start, type, subtype, comment, suggestion);
    }

    public ArticleReplacement withSuggestion(String suggestion) {
        if (this.suggestion == null ? suggestion == null : this.suggestion.equals(suggestion)) return this;
        return new ArticleReplacement(text, start, type, subtype, comment, suggestion);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ArticleReplacement that = (ArticleReplacement) obj;
        return start == that.start &&
                Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, start);
    }

    @Override
    public int compareTo(@NotNull ArticleReplacement o) {
        return o.start == start ? getEnd() - o.getEnd() : o.start - start;
    }

    @NonNls
    @Override
    public String toString() {
        return "ArticleReplacement{" +
                "text='" + text + '\'' +
                ", start=" + start +
                ", type=" + type +
                ", subtype='" + subtype + '\'' +
                ", comment='" + comment + '\'' +
                ", suggestion='" + suggestion + '\'' +
                '}';
    }

    /* ENTITY METHODS */

    boolean isContainedIn(ArticleReplacement replacement) {
        return start >= replacement.start && getEnd() <= replacement.getEnd();
    }

    boolean isContainedIn(Iterable<ArticleReplacement> replacements) {
        boolean isContained = false;
        for (ArticleReplacement replacement : replacements) {
            if (isContainedIn(replacement)) {
                isContained = true;
                break;
            }
        }
        return isContained;
    }

    boolean intersects(ArticleReplacement replacement) {
        return start > replacement.start && start < replacement.getEnd() && getEnd() > replacement.getEnd();
    }

    public static class ArticleReplacementBuilder {
        private String text;
        private int start;
        private ReplacementType type;
        private String subtype;
        private String comment;
        private String suggestion;

        public ArticleReplacement.ArticleReplacementBuilder setText(String text) {
            this.text = text;
            return this;
        }

        public ArticleReplacement.ArticleReplacementBuilder setStart(int start) {
            this.start = start;
            return this;
        }

        public ArticleReplacement.ArticleReplacementBuilder setType(ReplacementType type) {
            this.type = type;
            return this;
        }

        public ArticleReplacement.ArticleReplacementBuilder setSubtype(String subtype) {
            this.subtype = subtype;
            return this;
        }

        public ArticleReplacement.ArticleReplacementBuilder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public ArticleReplacement.ArticleReplacementBuilder setSuggestion(String suggestion) {
            this.suggestion = suggestion;
            return this;
        }

        public ArticleReplacement build() {
            return new ArticleReplacement(text, start, type, subtype, comment, suggestion);
        }
    }

}
