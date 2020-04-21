package es.bvalero.replacer.finder.benchmark.completetag;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.LinearIterable;
import es.bvalero.replacer.finder.LinearMatcher;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;

class CompleteTagLinearFinder implements BenchmarkFinder {
    private final Set<String> tags;

    CompleteTagLinearFinder(List<String> tags) {
        this.tags = new HashSet<>(tags);
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        return new HashSet<>(IterableUtils.toList(new LinearIterable<>(text, this::findResult, this::convert)));
    }

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
