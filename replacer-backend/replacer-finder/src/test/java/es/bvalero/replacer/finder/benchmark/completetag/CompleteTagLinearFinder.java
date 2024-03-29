package es.bvalero.replacer.finder.benchmark.completetag;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;

class CompleteTagLinearFinder implements BenchmarkFinder {

    private final Set<String> tags;

    CompleteTagLinearFinder(Set<String> tags) {
        this.tags = new HashSet<>(tags);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findCompleteTag);
    }

    @Nullable
    private MatchResult findCompleteTag(FinderPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            int startCompleteTag = findStartCompleteTag(text, start);
            if (startCompleteTag >= 0) {
                String tag = findSupportedTag(text, startCompleteTag + 1);
                if (tag != null) {
                    int endOpenTag = findEndTag(text, startCompleteTag + tag.length() + 1);
                    if (endOpenTag >= 0) {
                        int endCompleteTag = findEndCompleteTag(text, endOpenTag + 1, tag);
                        if (endCompleteTag >= 0) {
                            return FinderMatchResult.of(text, startCompleteTag, endCompleteTag);
                        } else {
                            start = endOpenTag + 1;
                        }
                    } else {
                        start = startCompleteTag + tag.length() + 1;
                    }
                } else {
                    start = startCompleteTag + 1;
                }
            } else {
                return null;
            }
        }
        return null;
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
