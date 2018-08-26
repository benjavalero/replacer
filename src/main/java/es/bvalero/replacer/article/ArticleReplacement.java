package es.bvalero.replacer.article;

import es.bvalero.replacer.utils.RegexMatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain class of a potential replacement in an article to be used in the front-end.
 */
public class ArticleReplacement extends RegexMatch {

    private PotentialErrorType type;
    private String subtype;
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

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
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

    // No real need to override the "equals" method as the one in the base class is still valid

}
