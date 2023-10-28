package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.regex.MatchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find some XML tags and all the content within, even other tags, e.g. `<code>An <span>example</span>.</code>`
 */
@Component
class CompleteTagFinder extends ImmutableCheckedFinder {

    private static final char START_TAG = '<';
    private static final char END_TAG = '>';
    private static final String END_SELF_CLOSING_TAG = "/>";
    private static final String START_CLOSING_TAG = "</";

    @Autowired
    private FinderProperties finderProperties;

    @Override
    public FinderPriority getPriority() {
        return FinderPriority.VERY_HIGH;
    }

    @Override
    public int getMaxLength() {
        return 5000;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // To match the content of the tag with a regex we cannot use a negated class,
        // as we want to capture other tags inside.
        // The alternative is using the classic .+? approach (the lazy modifier is needed),
        // but this modifier is only supported by the Regex matcher, which is quite slower.
        // Even with more than 10 tags, the faster approach with difference is the linear search in one-pass.
        // There is also another approach to find the opening tag with an automaton, but it is 10x slower anyway.
        return LinearMatchFinder.find(page, this::findCompleteTag);
    }

    @Nullable
    private MatchResult findCompleteTag(FinderPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final int startCompleteTag = findStartTag(text, start);
            if (startCompleteTag < 0) {
                return null;
            }

            final int startTagName = startCompleteTag + 1;
            final String tagName = findSupportedTag(text, startTagName);
            if (tagName == null) {
                // The tag name (if found) is not in the list
                start = startTagName;
                continue;
            }

            int endTagName = startTagName + tagName.length();
            int endOpenTag = findEndTag(text, endTagName);
            if (endOpenTag < 0) {
                // Open tag not closed
                final String message = String.format("Open tag %s not closed", tagName);
                FinderUtils.logFinderResult(page, startCompleteTag, endTagName, message);
                start = endTagName;
                continue;
            }

            final String openTag = text.substring(startCompleteTag, endOpenTag);
            if (isSelfClosingTag(openTag)) {
                start = endOpenTag;
                continue;
            }

            final int endCompleteTag = findEndCompleteTag(text, endOpenTag, tagName);
            if (endCompleteTag < 0) {
                // Tag not closed
                final String message = String.format("Tag %s not closed", tagName);
                FinderUtils.logFinderResult(page, startCompleteTag, endOpenTag, message);
                start = endOpenTag;
                continue;
            }

            return LinearMatchResult.of(text, startCompleteTag, endCompleteTag);
        }
        return null;
    }

    private int findStartTag(String text, int start) {
        return text.indexOf(START_TAG, start);
    }

    @Nullable
    private String findSupportedTag(String text, int start) {
        int i = start;
        for (; i < text.length(); i++) {
            if (!FinderUtils.isAscii(text.charAt(i))) {
                break;
            }
        }
        final String tag = text.substring(start, i);
        return this.finderProperties.getCompleteTags().contains(tag) ? tag : null;
    }

    private int findEndTag(String text, int start) {
        int endTag = text.indexOf(END_TAG, start);
        return endTag >= 0 ? endTag + 1 : -1; // 1 = end tag length
    }

    private boolean isSelfClosingTag(String tag) {
        return tag.endsWith(END_SELF_CLOSING_TAG);
    }

    private int findEndCompleteTag(String text, int start, String tag) {
        final String closeTag = START_CLOSING_TAG + tag + END_TAG;
        final int startCloseTag = text.indexOf(closeTag, start);
        return startCloseTag >= 0 ? startCloseTag + closeTag.length() : -1;
    }
}
