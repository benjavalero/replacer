package es.bvalero.replacer.finder.util;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.MatchResult;

/**
 * Simple implementation to create match results from a match start and end.
 * Its purpose is avoiding creating substrings as much as possible.
 * The original text is also used as a field because it is passed by reference.
 * We also support groups, to implement the capture of nested or convenient matches.
 */
public record FinderMatchRange(String text, int start, int end, List<MatchResult> groups) implements MatchResult {
    private FinderMatchRange(String text, int start, int end) {
        // By default, we assume the match does not contain groups to improve performance.
        this(text, start, end, List.of());
    }

    public static FinderMatchRange of(String text, int start, int end) {
        return new FinderMatchRange(text, start, end);
    }

    public static FinderMatchRange ofNested(String text, int start, int end) {
        return new FinderMatchRange(text, start, end, new ArrayList<>(4));
    }

    public static FinderMatchRange ofEmpty(String text, int start) {
        return new FinderMatchRange(text, start, start);
    }

    // To compare different implementations of MatchResult
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MatchResult that)) return false;
        return start == that.start() && Objects.equals(group(), that.group());
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, group());
    }

    public void addGroup(MatchResult group) {
        this.groups.add(group);
    }

    public MatchResult getGroup(int group) {
        return group == 0 ? this : this.groups.get(group - 1);
    }

    private int length() {
        return this.end - this.start;
    }

    @Override
    public int start(int group) {
        return getGroup(group).start();
    }

    @Override
    public int end(int group) {
        return getGroup(group).end();
    }

    @Override
    public String group() {
        return length() == 0 ? EMPTY : this.text.substring(this.start, this.end);
    }

    @Override
    public String group(int group) {
        return getGroup(group).group();
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
