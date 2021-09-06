package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinderPriority;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import javax.annotation.Resource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find some XML tags and all the content within, even other tags, e.g. `<code>An <span>example</span>.</code>`
 */
@Component
class CompleteTagFinder extends ImmutableCheckedFinder {

    @Resource
    private Set<String> completeTags;

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.VERY_HIGH;
    }

    @Override
    public int getMaxLength() {
        return 5000;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // Even with more than 10 tags, the faster approach with difference is the linear search in one-pass.
        return LinearMatchFinder.find(page, this::findResult);
    }

    @Nullable
    private MatchResult findResult(FinderPage page, int start) {
        List<MatchResult> matches = new ArrayList<>();
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findCompleteTag(page, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findCompleteTag(FinderPage page, int start, List<MatchResult> matches) {
        String text = page.getContent();
        int startCompleteTag = findStartCompleteTag(text, start);
        if (startCompleteTag >= 0) {
            int startOpenTag = startCompleteTag + 1;
            String tag = findSupportedTag(text, startOpenTag);
            if (tag == null) {
                // The tag found is not in the list. Continue.
                return startOpenTag;
            } else {
                int endOpenTag = findEndOpenTag(text, startOpenTag + tag.length());
                if (endOpenTag >= 0) {
                    endOpenTag++; // Move to the position next to the end of the open tag

                    // Check self-closing tags
                    if (text.substring(startOpenTag, endOpenTag).endsWith("/>")) {
                        return endOpenTag;
                    }

                    int endCompleteTag = findEndCompleteTag(text, endOpenTag, tag);
                    if (endCompleteTag >= 0) {
                        matches.add(
                            LinearMatchResult.of(startCompleteTag, text.substring(startCompleteTag, endCompleteTag))
                        );
                        return endCompleteTag;
                    } else {
                        // Tag not closed. Notify and continue.
                        logWarning(text, startCompleteTag, endOpenTag, page, "Tag not closed");
                        return endOpenTag;
                    }
                } else {
                    // Open tag not closed. Notify and continue.
                    logWarning(text, startCompleteTag, startOpenTag + tag.length(), page, "Open tag not closed");
                    return startOpenTag + tag.length();
                }
            }
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

    private int findEndOpenTag(String text, int start) {
        return text.indexOf('>', start);
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
