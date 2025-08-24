package es.bvalero.replacer.finder.benchmark.completetag;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

class CompleteTagLinearIteratedFinder implements BenchmarkFinder {

    private final Set<String> tags;

    CompleteTagLinearIteratedFinder(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        for (String tag : tags) {
            matches.addAll(findResults(text, tag));
        }
        return matches.stream();
    }

    private List<BenchmarkResult> findResults(String text, String tag) {
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final String openTag = String.format("<%s", tag);
        final String closeTag = String.format("</%s>", tag);
        int start = 0;
        while (start >= 0) {
            start = text.indexOf(openTag, start);
            if (start >= 0) {
                final int endOpenTag = text.indexOf('>', start + openTag.length());
                if (endOpenTag >= 0) {
                    final String openTagContent = text.substring(start, endOpenTag);
                    if (openTagContent.contains("/")) {
                        start += openTag.length();
                    } else {
                        final int startCloseTag = text.indexOf(closeTag, start + openTag.length());
                        if (startCloseTag >= 0) {
                            final int endCloseTag = startCloseTag + closeTag.length();
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
