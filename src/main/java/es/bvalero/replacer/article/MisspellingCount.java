package es.bvalero.replacer.article;

public class MisspellingCount {

    private String text;
    private long count;

    public MisspellingCount(String text, long count) {
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
