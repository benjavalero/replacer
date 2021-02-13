package es.bvalero.replacer.finder.benchmark.completetag;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.common.FinderPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;

class CompleteTagLinearFinder implements BenchmarkFinder {

    private final Set<String> tags;

    CompleteTagLinearFinder(Set<String> tags) {
        this.tags = new HashSet<>(tags);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, (page1, start) -> findResult(page1, start));
    }

    private MatchResult findResult(FinderPage page, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findCompleteTag(page.getContent(), start, matches);
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
                            LinearMatchResult.of(startCompleteTag, text.substring(startCompleteTag, endCompleteTag))
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
        return tags.contains(tag) ? tag : null;
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
