package es.bvalero.replacer.article;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.RegexMatchType;

import java.util.List;

public class ArticleReplacement extends RegexMatch {

    private List<String> proposedFixes;
    private String comment;
    private String fixedText;
    private boolean fixed = false;

    public ArticleReplacement() {
        super();
    }

    ArticleReplacement(int position, String originalText) {
        super(position, originalText);
    }

    public List<String> getProposedFixes() {
        return proposedFixes;
    }

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

    public void setFixedText(String fixedText) {
        this.fixedText = fixedText;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

}
