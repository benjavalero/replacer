package es.bvalero.replacer.finder;

import java.util.List;
import java.util.Objects;

public class MatchResult implements Comparable<MatchResult> {

    private int start;
    private String text;

    public MatchResult(int start, String text) {
        this.start = start;
        this.text = text;
    }

    public int getStart() {
        return start;
    }

    void setStart(int start) {
        this.start = start;
    }

    public String getText() {
        return text;
    }

    void setText(String text) {
        this.text = text;
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

    @Override
    public int compareTo(MatchResult o) {
        return o.start == start ? getEnd() - o.getEnd() : o.start - start;
    }

    boolean isContainedIn(List<MatchResult> matchResults) {
        boolean isContained = false;
        for (MatchResult matchResult : matchResults) {
            if (isContainedIn(matchResult)) {
                isContained = true;
                break;
            }
        }
        return isContained;
    }

    private boolean isContainedIn(MatchResult matchResult) {
        return start >= matchResult.start && getEnd() <= matchResult.getEnd();
    }

}
