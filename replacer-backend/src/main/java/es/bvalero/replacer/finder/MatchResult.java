package es.bvalero.replacer.finder;

import java.util.Objects;

public final class MatchResult {

    private int start;
    private String text;

    public MatchResult(int start, String text) {
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
        MatchResult matchResult = (MatchResult) o;
        return start == matchResult.start &&
                text.equals(matchResult.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, text);
    }

    @Override
    public String toString() {
        return "MatchResult{" +
                "start=" + start +
                ", text='" + text + '\'' +
                '}';
    }
}
