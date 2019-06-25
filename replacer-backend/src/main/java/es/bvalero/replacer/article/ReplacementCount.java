package es.bvalero.replacer.article;

public class ReplacementCount {

    private final String text;
    private long count;

    public ReplacementCount(String text, long count) {
        this.text = text;
        this.count = count;
    }

    public String getText() {
        return text;
    }

    public long getCount() {
        return count;
    }

    void setCount(long count) {
        this.count = count;
    }

}
