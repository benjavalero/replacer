package es.bvalero.replacer.utils;

import java.io.Serializable;
import java.util.List;

public class RegexMatch implements Comparable<RegexMatch>, Serializable {

    private int position;
    private String originalText;
    private RegexMatchType type;

    public RegexMatch() {
    }

    public RegexMatch(int position, String originalText) {
        this.position = position;
        this.originalText = originalText;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public RegexMatchType getType() {
        return type;
    }

    public void setType(RegexMatchType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegexMatch that = (RegexMatch) o;

        return position == that.position && originalText.equals(that.originalText);
    }

    @Override
    public int hashCode() {
        int result = position;
        result = 31 * result + originalText.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RegexMatch{" +
                "position=" + position +
                ", originalText='" + originalText + '\'' +
                ", type=" + type +
                '}';
    }

    @Override
    public int compareTo(RegexMatch r2) {
        return r2.getPosition() - this.getPosition();
    }

    public int getEnd() {
        return this.getPosition() + this.getOriginalText().length();
    }

    public boolean isContainedIn(List<RegexMatch> regexMatches) {
        boolean isContained = false;
        for (RegexMatch regexMatch : regexMatches) {
            if (isContainedIn(regexMatch)) {
                isContained = true;
                break;
            }
        }
        return isContained;
    }

    public boolean isContainedIn(RegexMatch interval2) {
        return this.getPosition() >= interval2.getPosition() && this.getEnd() <= interval2.getEnd();
    }

}
