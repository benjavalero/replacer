package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find categories, e. g. `[[Categoría:España]]`
 */
@Component
public class CategoryFinder implements ImmutableFinder {
    private static final List<String> SPACES = Arrays.asList("Categoría", "als");

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public Iterable<Immutable> find(String text) {
        return new LinearIterable<>(text, this::findResult, this::convert);
    }

    public MatchResult findResult(String text, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && matches.isEmpty()) {
            start = findCategory(text, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findCategory(String text, int start, List<MatchResult> matches) {
        int startCategory = findStartCategory(text, start);
        if (startCategory >= 0) {
            int startCategoryName = findStartCategoryName(text, startCategory + 2);
            if (startCategoryName >= 0) {
                int endCategory = findEndCategory(text, startCategoryName);
                if (endCategory >= 0) {
                    matches.add(LinearMatcher.of(startCategory, text.substring(startCategory, endCategory + 2)));
                    return endCategory + 2;
                } else {
                    return startCategoryName;
                }
            } else {
                return startCategory + 2;
            }
        } else {
            return -1;
        }
    }

    private int findStartCategory(String text, int start) {
        return text.indexOf("[[", start);
    }

    private int findStartCategoryName(String text, int start) {
        StringBuilder prefixBuilder = new StringBuilder();
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '|' || ch == ']') {
                // Not a file but a hyperlink
                return -1;
            } else if (ch == ':') {
                String prefix = prefixBuilder.toString();
                return SPACES.contains(prefix) && (i + 1 < text.length()) ? i + 1 : -1;
            } else {
                prefixBuilder.append(ch);
            }
        }
        return -1;
    }

    private int findEndCategory(String text, int start) {
        return text.indexOf("]]", start);
    }
}
