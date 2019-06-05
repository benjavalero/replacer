package es.bvalero.replacer.article;

public class ReplacementCount {

    private final String text;
    private final long count;

    public ReplacementCount(String text, long count) {
        this.text = text;
        this.count = count;
    }

    public String getText() {
        return text;
    }

    @SuppressWarnings("unused")
    public long getCount() {
        return count;
    }

}
