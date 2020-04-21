package es.bvalero.replacer.finder.benchmark.completetag;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class CompleteTagLinearIteratedFinder implements BenchmarkFinder {
    private final List<String> tags;

    CompleteTagLinearIteratedFinder(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        for (String tag : tags) {
            matches.addAll(findResults(text, tag));
        }
        return matches;
    }

    private Set<FinderResult> findResults(String text, String tag) {
        Set<FinderResult> matches = new HashSet<>();
        String openTag = String.format("<%s", tag);
        String closeTag = String.format("</%s>", tag);
        int start = 0;
        while (start >= 0) {
            start = text.indexOf(openTag, start);
            if (start >= 0) {
                int endOpenTag = text.indexOf('>', start + openTag.length());
                if (endOpenTag >= 0) {
                    String openTagContent = text.substring(start, endOpenTag);
                    if (openTagContent.contains("/")) {
                        start += openTag.length();
                    } else {
                        int startCloseTag = text.indexOf(closeTag, start + openTag.length());
                        if (startCloseTag >= 0) {
                            int endCloseTag = startCloseTag + closeTag.length();
                            matches.add(FinderResult.of(start, text.substring(start, endCloseTag)));
                            start = endCloseTag + 1;
                        } else {
                            start += openTag.length();
                        }
                    }
                } else {
                    start += openTag.length();
                }
            }
        }
        return matches;
    }
}
