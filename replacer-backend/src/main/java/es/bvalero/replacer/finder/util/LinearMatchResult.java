package es.bvalero.replacer.finder.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import lombok.ToString;

@ToString
public class LinearMatchResult implements MatchResult {

    private final String text;
    private final int start;

    // Use groups to implement nested matches
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
        String content = this.group();

        // Remove the content of the nested templates
        for (int i = this.groupCount() - 1; i >= 0; i--) {
            content =
                content.substring(0, this.start(i) - this.start()) + content.substring(this.end(i) - this.start());
        }

        return content;
    }
}
