package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Find some XML tags and all the content within, even other tags, e. g. `<code>An <span>example</span>.</code>`
 */
@Component
public class CompleteTagFinder implements ImmutableFinder {
    private static final Set<String> TAG_NAMES = new HashSet<>(
        Arrays.asList(
            "blockquote",
            "cite",
            "code",
            "math",
            "nowiki",
            "poem",
            "pre",
            "ref",
            "score",
            "source",
            "syntaxhighlight"
        )
    );

    @Override
    public Iterable<Immutable> find(String text) {
        List<Immutable> matches = new ArrayList<>(100);
        int start = 0;
        while (start >= 0) {
            start = findCompleteTag(text, start, matches);
        }
        return matches;
    }

    private int findCompleteTag(String text, int start, List<Immutable> matches) {
        int startCompleteTag = findStartCompleteTag(text, start);
        if (startCompleteTag >= 0) {
            String tag = findSupportedTag(text, startCompleteTag + 1);
            if (tag != null) {
                int endOpenTag = findEndTag(text, startCompleteTag + tag.length() + 1);
                if (endOpenTag >= 0) {
                    int endCompleteTag = findEndCompleteTag(text, endOpenTag + 1, tag);
                    if (endCompleteTag >= 0) {
                        matches.add(Immutable.of(startCompleteTag, text.substring(startCompleteTag, endCompleteTag)));
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
        return TAG_NAMES.contains(tag) ? tag : null;
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
        String closeTag = new StringBuilder("</").append(tag).append('>').toString();
        int startCloseTag = text.indexOf(closeTag, start);
        if (startCloseTag >= 0) {
            // Returns the position next to the end of the complete tag
            return startCloseTag + closeTag.length();
        } else {
            return -1;
        }
    }
}
