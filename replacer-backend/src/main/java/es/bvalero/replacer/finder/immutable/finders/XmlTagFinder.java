package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.Set;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find XML tags, e.g. `<span>` or `<br />`
 */
@Component
class XmlTagFinder extends ImmutableCheckedFinder {

    private static final char START_TAG = '<';
    private static final char END_TAG = '>';
    private static final char END_TAG_SLASH = '/';

    // We want to avoid the XML comments to be captured by this
    private static final Set<Character> FORBIDDEN_CHARS = Set.of('#', '{', '}', START_TAG, END_TAG);

    @Override
    public int getMaxLength() {
        return 500;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return LinearMatchFinder.find(page, this::findTag);
    }

    @Nullable
    MatchResult findTag(WikipediaPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final int startTag = findStartTag(text, start);
            if (startTag >= 0) {
                final int startTagContent = startTag + 1; // 1 = start tag length
                final char firstChar = text.charAt(startTagContent);
                if (Character.isLetter(firstChar) || firstChar == END_TAG_SLASH) {
                    final int endTagContent = findEndTag(text, startTagContent);
                    if (endTagContent >= 0) {
                        final int endTag = endTagContent + 1; // 1 = end tag length
                        final String tagContent = text.substring(startTagContent, endTagContent);
                        if (isValidTagContent(tagContent)) {
                            return LinearMatchResult.of(startTag, text.substring(startTag, endTag));
                        } else {
                            start = endTag;
                        }
                    } else {
                        // Not an XML tag
                        start = startTagContent;
                    }
                } else {
                    // Not an XML tag
                    start = startTagContent;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    private int findStartTag(String text, int start) {
        return text.indexOf(START_TAG, start);
    }

    private int findEndTag(String text, int start) {
        return text.indexOf(END_TAG, start);
    }

    private boolean isValidTagContent(String tagContent) {
        for (int i = 0; i < tagContent.length(); i++) {
            if (isForbiddenChar(tagContent.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isForbiddenChar(char ch) {
        return FORBIDDEN_CHARS.contains(ch);
    }
}
