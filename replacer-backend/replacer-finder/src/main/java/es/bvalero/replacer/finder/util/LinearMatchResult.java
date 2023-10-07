package es.bvalero.replacer.finder.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class LinearMatchResult implements MatchResult {

    private final String text;
    private final int start;

    // Use groups to implement nested matches
    @Getter
    @EqualsAndHashCode.Exclude
    private final List<LinearMatchResult> groups = new ArrayList<>();

    private LinearMatchResult(String text, int start) {
        this.text = text;
        this.start = start;
    }

    public static LinearMatchResult of(int start, String text) {
        return new LinearMatchResult(text, start);
    }

    public void addGroups(List<LinearMatchResult> groups) {
        this.groups.addAll(groups);
    }

    public void addGroup(LinearMatchResult group) {
        this.groups.add(group);
    }

    public Iterable<String> getGroupValues() {
        return this.groups.stream().map(LinearMatchResult::group).toList();
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

    public String getTextWithoutNested() {
        String content = group();

        // Remove the content of the nested templates
        for (int i = groupCount() - 1; i >= 0; i--) {
            content = content.substring(0, start(i) - start()) + content.substring(end(i) - start());
        }

        return content;
    }
}
