package es.bvalero.replacer.finder.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import lombok.*;
import org.jetbrains.annotations.TestOnly;

/**
 * Simple implementation to create match results from a match start and text.
 * We also support groups, to implement the capture of nested or convenient matches.
 */
@ToString
@EqualsAndHashCode
public class FinderMatchResult implements MatchResult {

    private final int start;

    @Setter(AccessLevel.PROTECTED) // Just for the temporary matches when capturing nested groups
    private String text;

    @Getter(onMethod_ = @TestOnly)
    @EqualsAndHashCode.Exclude
    private final List<MatchResult> groups = new ArrayList<>();

    protected FinderMatchResult(int start, String text) {
        this.start = start;
        this.text = text;
        // Group 0 is the whole match, but we don't actually need to add it.
        this.groups.add(null);
    }

    public static FinderMatchResult of(int start, String text) {
        return new FinderMatchResult(start, text);
    }

    public static FinderMatchResult of(String text, int start, int end) {
        return FinderMatchResult.of(start, text.substring(start, end));
    }

    public void addGroup(MatchResult group) {
        this.groups.add(group);
    }

    @Override
    public int start() {
        return this.start;
    }

    @Override
    public int start(int group) {
        return group == 0 ? this.start() : this.groups.get(group).start();
    }

    @Override
    public int end() {
        return this.start + this.text.length();
    }

    @Override
    public int end(int group) {
        return group == 0 ? this.end() : this.groups.get(group).end();
    }

    @Override
    public String group() {
        return this.text;
    }

    @Override
    public String group(int group) {
        return group == 0 ? this.group() : this.groups.get(group).group();
    }

    @Override
    public int groupCount() {
        return this.groups.size() - 1;
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
