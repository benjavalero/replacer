package es.bvalero.replacer.finder.benchmark.completetag;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.HashSet;
import java.util.Set;

class CompleteTagLinearIteratedFinder implements BenchmarkFinder {

    private final Set<String> tags;

    CompleteTagLinearIteratedFinder(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public Set<BenchmarkResult> findMatches(WikipediaPage page) {
        String text = page.getContent();
        Set<BenchmarkResult> matches = new HashSet<>();
        for (String tag : tags) {
            matches.addAll(findResults(text, tag));
        }
        return matches;
    }

    private Set<BenchmarkResult> findResults(String text, String tag) {
        Set<BenchmarkResult> matches = new HashSet<>();
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
                            matches.add(BenchmarkResult.of(start, text.substring(start, endCloseTag)));
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
