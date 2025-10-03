package es.bvalero.replacer.finder.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.MatchResult;

/**
 * Simple implementation to create match results from a match start and text.
 * We also support groups, to implement the capture of nested or convenient matches.
 */
public record FinderMatchResult(int start, String group, List<MatchResult> groups) implements MatchResult {
    private FinderMatchResult(int start, String text) {
        // By default, we assume the match does not contain groups to improve performance.
        this(start, text, List.of());
    }

    public static FinderMatchResult of(int start, String text) {
        return new FinderMatchResult(start, text);
    }

    public static FinderMatchResult ofNested(int start, String text) {
        return new FinderMatchResult(start, text, new ArrayList<>(4));
    }

    // To compare different implementations of MatchResult
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MatchResult that)) return false;
        return start == that.start() && Objects.equals(group, that.group());
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, group);
    }

    public void addGroup(MatchResult group) {
        this.groups.add(group);
    }

    @Override
    public int start(int group) {
        return group == 0 ? this.start : this.groups.get(group - 1).start();
    }

    @Override
    public int end() {
        return this.start + this.group.length();
    }

    @Override
    public int end(int group) {
        return group == 0 ? this.end() : this.groups.get(group - 1).end();
    }

    @Override
    public String group(int group) {
        return group == 0 ? this.group() : this.groups.get(group - 1).group();
    }

    @Override
    public int groupCount() {
        return this.groups.size();
    }

    public boolean containsNested(int position) {
        for (MatchResult m : this.groups) {
            if (m != null && position >= m.start() && position < m.end()) {
                return true;
            }
        }
        return false;
    }
}
