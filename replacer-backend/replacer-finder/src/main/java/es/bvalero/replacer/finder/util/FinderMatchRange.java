package es.bvalero.replacer.finder.util;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Implementation of {@link MatchResult} based on start/end positions over the original page text.
 * The original text is stored by reference, and {@code group()} is lazy and cached.
 * We also support groups, to implement the capture of nested or convenient matches.
 */
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(of = { "start", "end" })
public class FinderMatchRange implements MatchResult {

    private final String text;
    private final int start;
    private final int end;
    private final List<MatchResult> groups;
    private String cachedGroup;

    private FinderMatchRange(String text, int start, int end, List<MatchResult> groups, String cachedGroup) {
        this.text = text;
        this.start = start;
        this.end = end;
        this.groups = groups;
        this.cachedGroup = cachedGroup;
    }

    private FinderMatchRange(String text, int start, int end) {
        this(text, start, end, List.of(), null);
    }

    public static FinderMatchRange of(String text, int start, int end) {
        return new FinderMatchRange(text, start, end);
    }

    public static FinderMatchRange ofNested(String text, int start, int end) {
        return new FinderMatchRange(text, start, end, new ArrayList<>(4), null);
    }

    public static FinderMatchRange ofEmpty(String text, int start) {
        return new FinderMatchRange(text, start, start);
    }

    /** Use when the substring is already available to avoid recomputing it. */
    public static FinderMatchRange of(String text, int start, String knownGroup) {
        return new FinderMatchRange(text, start, start + knownGroup.length(), List.of(), knownGroup);
    }

    /** Use when the substring is already available and groups are needed. */
    public static FinderMatchRange ofNested(String text, int start, String knownGroup) {
        return new FinderMatchRange(text, start, start + knownGroup.length(), new ArrayList<>(4), knownGroup);
    }

    public void addGroup(MatchResult group) {
        this.groups.add(group);
    }

    public MatchResult getGroup(int group) {
        return group == 0 ? this : this.groups.get(group - 1);
    }

    @Override
    public int start() {
        return start;
    }

    @Override
    public int start(int group) {
        return getGroup(group).start();
    }

    @Override
    public int end() {
        return end;
    }

    @Override
    public int end(int group) {
        return getGroup(group).end();
    }

    @Override
    public String group() {
        if (cachedGroup == null) {
            cachedGroup = (end - start) == 0 ? EMPTY : text.substring(start, end);
        }
        return cachedGroup;
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
