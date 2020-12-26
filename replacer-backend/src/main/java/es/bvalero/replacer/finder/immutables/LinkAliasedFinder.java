package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.page.IndexablePage;
import java.util.*;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find the first part of aliased links, e.g. `brasil` in `[[brasil|Brasil]]`.
 * It also finds categories, files, etc.
 */
@Component
public class LinkAliasedFinder implements ImmutableFinder {

    private static final Set<Character> FORBIDDEN_CHARS = new HashSet<>(Arrays.asList(']', '|', '\n'));

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 250;
    }

    @Override
    public Iterable<Immutable> find(IndexablePage page) {
        return new LinearIterable<>(page, this::findResult, this::convert);
    }

    @Nullable
    public MatchResult findResult(IndexablePage page, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findLinkAliased(page.getContent(), start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findLinkAliased(String text, int start, List<MatchResult> matches) {
        int startTemplateParam = findStartLink(text, start);
        if (startTemplateParam >= 0) {
            int endParam = findEndLink(text, startTemplateParam + 2);
            if (endParam >= 0) {
                matches.add(LinearMatcher.of(startTemplateParam + 2, text.substring(startTemplateParam + 2, endParam)));
                return endParam;
            } else {
                return startTemplateParam + 2;
            }
        } else {
            return -1;
        }
    }

    private int findStartLink(String text, int start) {
        return text.indexOf("[[", start);
    }

    private int findEndLink(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '|') {
                return i == start ? -1 : i;
            } else if (!isLinkChar(ch)) {
                return -1;
            }
        }
        return -1;
    }

    private boolean isLinkChar(char ch) {
        return !FORBIDDEN_CHARS.contains(ch);
    }
}
