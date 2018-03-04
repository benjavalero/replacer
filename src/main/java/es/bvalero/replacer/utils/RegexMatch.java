package es.bvalero.replacer.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegexMatch implements Comparable<RegexMatch> {

    private int position;
    private String originalText;

    public RegexMatch() {
    }

    public RegexMatch(int position, String originalText) {
        this.position = position;
        this.originalText = originalText;
    }

    public static List<RegexMatch> removedNestedMatches(List<RegexMatch> matches) {
        Collections.sort(matches);

        boolean[] toDelete = new boolean[matches.size()];
        for (int i = 0; i < matches.size(); i++) {
            for (int j = i + 1; j < matches.size(); j++) {
                if (matches.get(i).isContainedIn(matches.get(j))) {
                    toDelete[i] = true;
                }
            }
        }

        List<RegexMatch> resultMatches = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            if (!toDelete[i]) {
                resultMatches.add(matches.get(i));
            }
        }

        return resultMatches;
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
    public int compareTo(@NotNull RegexMatch match) {
        if (match.getPosition() != this.getPosition()) {
            return match.getPosition() - this.getPosition();
        } else {
            return this.getEnd() - match.getEnd();
        }
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
