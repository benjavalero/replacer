package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import java.util.*;
import java.util.regex.MatchResult;
import javax.annotation.Resource;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import org.springframework.stereotype.Component;

@Component
public class TemplateParamFinder implements ImmutableFinder {
    private static final Set<Character> ALLOWED_CHARS = new HashSet<>(Arrays.asList('-', '_'));

    @Resource
    private List<String> paramNames;

    @Resource
    private List<String> paramValues;

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.HIGH;
    }

    @Override
    public int getMaxLength() {
        return 100;
    }

    @Override
    public Iterable<Immutable> find(String text, WikipediaLanguage lang) {
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
        int startParam = findStartParam(text, start);
        if (startParam >= 0) {
            int posEquals = findPosEquals(text, startParam + 1);
            if (posEquals >= 0) {
                int endParam = findEndParam(text, posEquals + 1);
                if (endParam >= 0) {
                    matches.add(createMatch(text, startParam + 1, posEquals, endParam));
                    return endParam + 1;
                } else {
                    return posEquals + 1;
                }
            } else {
                return startParam + 1;
            }
        } else {
            return -1;
        }
    }

    private int findStartParam(String text, int start) {
        return text.indexOf('|', start);
    }

    private int findPosEquals(String text, int start) {
        // The start of the parameter '|' could be followed by a dash '-' in tables
        if (start < text.length() && text.charAt(start) == '-') {
            return -1;
        }

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

    private int findEndParam(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '|' || ch == '}') {
                return i;
            } else if (ch == '"') {
                // Forbidden value chars
                return -1;
            }
        }
        return -1;
    }

    private boolean isParamChar(char ch) {
        return Character.isLetterOrDigit(ch) || Character.isWhitespace(ch) || ALLOWED_CHARS.contains(ch);
    }

    private LinearMatcher createMatch(String text, int start, int posEquals, int end) {
        String paramName = text.substring(start, posEquals);
        if (paramNames.contains(paramName.trim())) {
            // If the param name is in the list, return the complete match
            return LinearMatcher.of(start, text.substring(start, end));
        } else {
            String paramValue = text.substring(posEquals + 1, end).trim();
            if (paramValues.contains(paramValue)) {
                // If the value is in the list, return the complete match
                return LinearMatcher.of(start, text.substring(start, end));
            } else if (matchesFile(paramValue)) {
                // If the value matches a file or a domain, return the complete match
                return LinearMatcher.of(start, text.substring(start, end));
            } else {
                // Else return only the param
                return LinearMatcher.of(start, paramName);
            }
        }
    }

    private boolean matchesFile(String value) {
        int dot = value.lastIndexOf('.');
        int distanceToEnd = value.length() - dot;
        return distanceToEnd >= 2 && distanceToEnd <= 4;
    }
}
