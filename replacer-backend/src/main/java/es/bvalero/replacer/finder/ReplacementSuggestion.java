package es.bvalero.replacer.finder;

public final class ReplacementSuggestion {

    private String text;
    private String comment;

    public ReplacementSuggestion(String text, String comment) {
        this.text = text;
        this.comment = comment;
    }

    public String getText() {
        return text;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return "ReplacementSuggestion{" +
                "text='" + text + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
