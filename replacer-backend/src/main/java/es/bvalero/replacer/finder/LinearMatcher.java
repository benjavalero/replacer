package es.bvalero.replacer.finder;

import java.util.regex.MatchResult;
import lombok.ToString;

@ToString
public class LinearMatcher implements MatchResult {
    private String text;
    private int start;

    private LinearMatcher(String text, int start) {
        this.text = text;
        this.start = start;
    }

    public static LinearMatcher of(int start, String text) {
        return new LinearMatcher(text, start);
    }

    @Override
    public int start() {
        return this.start;
    }

    @Override
    public int start(int group) {
        return this.start();
    }

    @Override
    public int end() {
        return this.start + this.text.length();
    }

    @Override
    public int end(int group) {
        return this.end();
    }

    @Override
    public String group() {
        return this.text;
    }

    @Override
    public String group(int group) {
        return this.group();
    }

    @Override
    public int groupCount() {
        return 0;
    }
}
