package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import java.util.*;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

@Component
public class TemplateParamFinder implements ImmutableFinder {
    private static final Set<Character> ALLOWED_CHARS = new HashSet<>(Arrays.asList('-', '_'));

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.HIGH;
    }

    @Override
    public int getMaxLength() {
        return 50;
    }

    @Override
    public Iterable<Immutable> find(String text) {
        return new LinearIterable<>(text, this::findResult, this::convert);
    }

    public MatchResult findResult(String text, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && matches.isEmpty()) {
            start = findTemplateParam(text, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findTemplateParam(String text, int start, List<MatchResult> matches) {
        int startTemplateParam = findStartTemplateParam(text, start);
        if (startTemplateParam >= 0) {
            int endParam = findEndParam(text, startTemplateParam + 1);
            if (endParam >= 0) {
                matches.add(LinearMatcher.of(startTemplateParam + 1, text.substring(startTemplateParam + 1, endParam)));
                return endParam;
            } else {
                return startTemplateParam + 1;
            }
        } else {
            return -1;
        }
    }

    private int findStartTemplateParam(String text, int start) {
        return text.indexOf('|', start);
    }

    private int findEndParam(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '=') {
                return i == start ? -1 : i;
            } else if (!isParamChar(ch)) {
                return -1;
            }
        }
        return -1;
    }

    private boolean isParamChar(char ch) {
        return Character.isLetterOrDigit(ch) || Character.isWhitespace(ch) || ALLOWED_CHARS.contains(ch);
    }
}
