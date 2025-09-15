package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.NEW_LINE;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
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

    @Override
    public int getMaxLength() {
        return 500;
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findTag);
    }

    @Nullable
    private MatchResult findTag(FinderPage page, int start) {
        final String text = page.getContent();
        // TODO: Reduce cyclomatic complexity
        while (start >= 0 && start < text.length()) {
            final int startTag = findStartTag(text, start);
            if (startTag < 0) {
                return null;
            }

            final int startTagContent = startTag + 1;
            if (!isValidFirstChar(text.charAt(startTagContent))) {
                // Not an XML tag
                start = startTagContent;
                continue;
            }

            final int endTagContent = findEndTag(text, startTagContent);
            if (endTagContent < 0) {
                // Not an XML tag
                start = startTagContent;
                continue;
            }

            final int endTag = endTagContent + 1;
            final String tagContent = text.substring(startTagContent, endTagContent);
            if (!isValidTagContent(tagContent)) {
                start = endTag;
                continue;
            }

            return FinderMatchResult.of(text, startTag, endTag);
        }
        return null;
    }

    private int findStartTag(String text, int start) {
        return text.indexOf(START_TAG, start);
    }

    private int findEndTag(String text, int start) {
        return text.indexOf(END_TAG, start);
    }

    private boolean isValidFirstChar(char ch) {
        return Character.isLetter(ch) || ch == END_TAG_SLASH;
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
        // We want to avoid some mathematical expressions with the "less than" sign
        return ch == NEW_LINE || ch == START_TAG || ch == END_TAG;
    }
}
