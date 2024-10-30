package es.bvalero.replacer.finder.benchmark.completetag;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

class CompleteTagRegexIteratedFinder implements BenchmarkFinder {

    private final List<Pattern> patterns = new ArrayList<>();

    CompleteTagRegexIteratedFinder(Set<String> tags) {
        patterns.addAll(
            tags
                .stream()
                .map(tag -> Pattern.compile(String.format("<%s[^>/]*?>.+?</%s>", tag, tag), Pattern.DOTALL))
                .toList()
        );
    }

    @Override
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        Set<BenchmarkResult> results = new HashSet<>();
        for (Pattern pattern : patterns) {
            for (MatchResult matchResult : RegexMatchFinder.find(text, pattern)) {
                results.add(convert(matchResult, page));
            }
        }
        return results;
    }
}
