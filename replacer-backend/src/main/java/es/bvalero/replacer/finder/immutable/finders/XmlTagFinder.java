package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find XML tags, e.g. `<span>` or `<br />`
 */
@Component
class XmlTagFinder extends ImmutableCheckedFinder {

    // We want to avoid the XML comments to be captured by this
    private static final Set<Character> FORBIDDEN_CHARS = Set.of('#', '{', '}', '<', '>');

    @Override
    public int getMaxLength() {
        return 500;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return LinearMatchFinder.find(page, this::findTag);
    }

    private int findTag(WikipediaPage page, int start, List<MatchResult> matches) {
        final String text = page.getContent();
        final int startTag = findStartTag(text, start);
        if (startTag >= 0) {
            final char first = text.charAt(startTag + 1);
            if (Character.isLetter(first) || first == '/') {
                final int endTag = findEndTag(text, startTag + 1);
                if (endTag >= 0) {
                    final String tag = text.substring(startTag + 1, endTag);
                    if (validateTagChars(tag)) {
                        matches.add(LinearMatchResult.of(startTag, text.substring(startTag, endTag + 1)));
                    }
                    return endTag + 1;
                } else {
                    // Not an XML tag
                    return startTag + 1;
                }
            } else {
                // Not an XML tag
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
