package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import java.util.*;
import java.util.regex.MatchResult;
import javax.annotation.Resource;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find some XML tags and all the content within, even other tags, e.g. `<code>An <span>example</span>.</code>`
 */
@Component
public class CompleteTagFinder implements ImmutableFinder {
    @Resource
    private Set<String> completeTags;

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.VERY_HIGH;
    }

    @Override
    public int getMaxLength() {
        return 50000;
    }

    @Override
    public Iterable<Immutable> find(String text, WikipediaLanguage lang) {
        return new LinearIterable<>(text, this::findResult, this::convert);
    }

    @Nullable
    public MatchResult findResult(String text, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && matches.isEmpty()) {
            start = findCompleteTag(text, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findCompleteTag(String text, int start, List<MatchResult> matches) {
        int startCompleteTag = findStartCompleteTag(text, start);
        if (startCompleteTag >= 0) {
            String tag = findSupportedTag(text, startCompleteTag + 1);
            if (tag != null) {
                int endOpenTag = findEndTag(text, startCompleteTag + tag.length() + 1);
                if (endOpenTag >= 0) {
                    int endCompleteTag = findEndCompleteTag(text, endOpenTag + 1, tag);
                    if (endCompleteTag >= 0) {
                        matches.add(
                            LinearMatcher.of(startCompleteTag, text.substring(startCompleteTag, endCompleteTag))
                        );
                        return endCompleteTag;
                    } else {
                        return endOpenTag + 1;
                    }
                } else {
                    return startCompleteTag + tag.length() + 1;
                }
            }
            return startCompleteTag + 1;
        } else {
            return -1;
        }
    }

    private int findStartCompleteTag(String text, int start) {
        return text.indexOf('<', start);
    }

    @Nullable
    private String findSupportedTag(String text, int start) {
        StringBuilder tagBuilder = new StringBuilder();
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (FinderUtils.isAscii(ch)) {
                tagBuilder.append(ch);
            } else {
                break;
            }
        }
        String tag = tagBuilder.toString();
        return completeTags.contains(tag) ? tag : null;
    }

    private int findEndTag(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '/') {
                // Avoid cases like <br />
                return -1;
            } else if (ch == '>') {
                return i;
            }
        }
        return -1;
    }

    private int findEndCompleteTag(String text, int start, String tag) {
        String closeTag = "</" + tag + '>';
        int startCloseTag = text.indexOf(closeTag, start);
        if (startCloseTag >= 0) {
            // Returns the position next to the end of the complete tag
            return startCloseTag + closeTag.length();
        } else {
            return -1;
        }
    }
}
