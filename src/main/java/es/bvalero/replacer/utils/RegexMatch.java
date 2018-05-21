package es.bvalero.replacer.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
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

    // It doesn't merge intersecting matches!
    public static List<RegexMatch> removedNestedMatches(List<RegexMatch> matches) {
        // To remove from Iterator we use a LinkedList
        List<RegexMatch> sortedMatches = new LinkedList<>(matches);
        if (sortedMatches.isEmpty()) {
            return sortedMatches;
        }
        Collections.sort(sortedMatches, Collections.<RegexMatch>reverseOrder());
        Iterator<RegexMatch> it = sortedMatches.iterator();
        RegexMatch previous = it.next();
        while (it.hasNext()) {
            RegexMatch current = it.next();
            if (current.isContainedIn(previous)) {
                it.remove();
            } else if (current.intersects(previous)) {
                // Merge previous and current
                previous.setOriginalText(previous.getOriginalText().substring(0, current.getEnd() - previous.getEnd())
                        + current.getOriginalText());
                it.remove();
            } else {
                previous = current;
            }
        }

        Collections.sort(sortedMatches);
        return sortedMatches;
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

    private boolean isContainedIn(RegexMatch interval2) {
        return this.getPosition() >= interval2.getPosition() && this.getEnd() <= interval2.getEnd();
    }

    private boolean intersects(RegexMatch interval2) {
        return this.getPosition() > interval2.getPosition() && this.getPosition() < interval2.getEnd() && this.getEnd() > interval2.getEnd();
    }

}
