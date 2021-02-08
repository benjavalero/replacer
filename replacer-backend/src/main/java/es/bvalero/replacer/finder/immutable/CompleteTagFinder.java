package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import es.bvalero.replacer.page.IndexablePage;
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
    int getMaxLength() {
        return 5000;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(IndexablePage page) {
        // Even with several tags taken into account the faster approach is the linear search in one-pass
        return LinearMatchFinder.find(page, this::findResult);
    }

    @Nullable
    private MatchResult findResult(IndexablePage page, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findCompleteTag(page.getContent(), start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findCompleteTag(String text, int start, List<MatchResult> matches) {
        int startCompleteTag = findStartCompleteTag(text, start);
        if (startCompleteTag >= 0) {
            int startOpenTag = startCompleteTag + 1;
            String tag = findSupportedTag(text, startOpenTag);
            if (tag == null) {
                return startOpenTag;
            } else {
                int endOpenTag = findEndTag(text, startOpenTag + tag.length());
                if (endOpenTag >= 0) {
                    // Check cases like <br />
                    if (text.substring(startOpenTag, endOpenTag).contains("/")) {
                        return endOpenTag + 1;
                    }

                    int endCompleteTag = findEndCompleteTag(text, endOpenTag + 1, tag);
                    if (endCompleteTag >= 0) {
                        matches.add(
                            LinearMatchResult.of(startCompleteTag, text.substring(startCompleteTag, endCompleteTag))
                        );
                        return endCompleteTag;
                    } else {
                        return endOpenTag + 1;
                    }
                } else {
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

    private int findEndTag(String text, int start) {
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
