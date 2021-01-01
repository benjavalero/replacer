package es.bvalero.replacer.finder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import lombok.ToString;

@ToString
public class LinearMatcher implements MatchResult {

    private final String text;
    private final int start;

    // Use groups to implement nested matches
    private final List<LinearMatcher> groups = new ArrayList<>();

    private LinearMatcher(String text, int start) {
        this.text = text;
        this.start = start;
    }

    public static LinearMatcher of(int start, String text) {
        return new LinearMatcher(text, start);
    }

    public void addGroups(List<LinearMatcher> groups) {
        this.groups.addAll(groups);
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
}
