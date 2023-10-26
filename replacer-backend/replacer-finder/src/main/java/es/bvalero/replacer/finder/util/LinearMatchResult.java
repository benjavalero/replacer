package es.bvalero.replacer.finder.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Simple implementation to create specific match results, in particular using linear finders.
 * Actually, it should not be named "linear", as it is used as a result from other algorithms.
 */
@ToString
@EqualsAndHashCode
public class LinearMatchResult implements MatchResult {

    private final int start;

    @Setter
    private String text;

    // Use groups to implement nested matches
    @Getter
    @EqualsAndHashCode.Exclude
    private final List<LinearMatchResult> groups = new ArrayList<>();

    protected LinearMatchResult(int start, String text) {
        this.start = start;
        this.text = text;
    }

    public static LinearMatchResult of(int start, String text) {
        return new LinearMatchResult(start, text);
    }

    public static LinearMatchResult ofEmpty(int start) {
        // Temporary empty text
        return new LinearMatchResult(start, "");
    }

    public static LinearMatchResult of(String text, int start, int end) {
        return new LinearMatchResult(start, text.substring(start, end));
    }

    public void addGroup(LinearMatchResult group) {
        this.groups.add(group);
    }

    @Override
    public int start() {
        return this.start;
    }

    @Override
    public int start(int group) {
        return this.groups.get(group).start();
    }

    @Override
    public int end() {
        return this.start + this.text.length();
    }

    @Override
    public int end(int group) {
        return this.groups.get(group).end();
    }

    @Override
    public String group() {
        return this.text;
    }

    @Override
    public String group(int group) {
        return this.groups.get(group).group();
    }

    @Override
    public int groupCount() {
        return this.groups.size();
    }

    public boolean containsNested(int position) {
        return this.groups.stream().anyMatch(m -> m.contains(position));
    }

    private boolean contains(int position) {
        return position >= this.start && position <= this.end();
    }
}
