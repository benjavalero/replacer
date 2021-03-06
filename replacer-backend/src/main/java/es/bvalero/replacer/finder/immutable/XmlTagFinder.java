package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.*;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find XML tags, e.g. `<span>` or `<br />`
 */
@Component
class XmlTagFinder extends ImmutableCheckedFinder {

    // We want to avoid the XML comments to be captured by this
    private static final Set<Character> FORBIDDEN_CHARS = new HashSet<>(Arrays.asList('#', '{', '}', '<', '>'));

    @Override
    int getMaxLength() {
        return 500;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findResult);
    }

    @Nullable
    private MatchResult findResult(FinderPage page, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findTag(page.getContent(), start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findTag(String text, int start, List<MatchResult> matches) {
        int startTag = findStartTag(text, start);
        if (startTag >= 0) {
            char first = text.charAt(startTag + 1);
            if (Character.isLetter(first) || first == '/') {
                int endTag = findEndTag(text, startTag + 1);
                if (endTag >= 0) {
                    String tag = text.substring(startTag + 1, endTag);
                    if (validateTagChars(tag)) {
                        matches.add(LinearMatchResult.of(startTag, text.substring(startTag, endTag + 1)));
                    }
                    return endTag + 1;
                } else {
                    return startTag + 1;
                }
            } else {
                return startTag + 1;
            }
        } else {
            return -1;
        }
    }

    private int findStartTag(String text, int start) {
        return text.indexOf('<', start);
    }

    private int findEndTag(String text, int start) {
        return text.indexOf('>', start);
    }

    private boolean validateTagChars(String tag) {
        for (int i = 0; i < tag.length(); i++) {
            if (FORBIDDEN_CHARS.contains(tag.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
