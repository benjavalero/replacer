package es.bvalero.replacer.misspelling;

import java.util.Objects;

public class WordMatch {

    private int start;
    private String text;

    WordMatch(int start, String text) {
        this.start = start;
        this.text = text;
    }

    int getStart() {
        return start;
    }

    String getText() {
        return text;
    }

    int getEnd() {
        return this.start + this.text.length();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordMatch wordMatch = (WordMatch) o;
        return start == wordMatch.start &&
                text.equals(wordMatch.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, text);
    }

    @Override
    public String toString() {
        return "WordMatch{" +
                "start=" + start +
                ", text='" + text + '\'' +
                '}';
    }
}
