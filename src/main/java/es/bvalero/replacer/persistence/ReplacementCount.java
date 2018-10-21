package es.bvalero.replacer.persistence;

@SuppressWarnings("unused")
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

    public long getCount() {
        return count;
    }

}
