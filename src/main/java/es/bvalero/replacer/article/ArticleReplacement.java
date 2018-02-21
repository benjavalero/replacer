package es.bvalero.replacer.article;

import es.bvalero.replacer.utils.RegexMatch;

import java.util.ArrayList;
import java.util.List;

public class ArticleReplacement extends RegexMatch {

    private PotentialErrorType type;
    private List<String> proposedFixes = new ArrayList<>();
    private String comment;
    private String fixedText;
    private boolean fixed = false;

    public ArticleReplacement() {
        super();
    }

    ArticleReplacement(int position, String originalText) {
        super(position, originalText);
    }

    ArticleReplacement(RegexMatch regexMatch) {
        super(regexMatch.getPosition(), regexMatch.getOriginalText());
    }

    public PotentialErrorType getType() {
        return type;
    }

    public void setType(PotentialErrorType type) {
        this.type = type;
    }

    public List<String> getProposedFixes() {
        return proposedFixes;
    }

    @SuppressWarnings("unused")
    public void setProposedFixes(List<String> proposedFixes) {
        this.proposedFixes = proposedFixes;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFixedText() {
        return fixedText;
    }

    @SuppressWarnings("unused")
    public void setFixedText(String fixedText) {
        this.fixedText = fixedText;
    }

    public boolean isFixed() {
        return fixed;
    }

    @SuppressWarnings("unused")
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArticleReplacement that = (ArticleReplacement) o;

        return getPosition() == that.getPosition() && getOriginalText().equals(that.getOriginalText());
    }

    @Override
    public int hashCode() {
        int result = getPosition();
        result = 31 * result + getOriginalText().hashCode();
        return result;
    }

}
