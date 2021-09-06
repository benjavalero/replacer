package es.bvalero.replacer.finder.benchmark.completetag;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class CompleteTagRegexIteratedFinder implements BenchmarkFinder {

    private final List<Pattern> patterns = new ArrayList<>();

    CompleteTagRegexIteratedFinder(Set<String> tags) {
        patterns.addAll(
            tags
                .stream()
                .map(tag -> Pattern.compile(String.format("<%s[^>/]*?>.+?</%s>", tag, tag), Pattern.DOTALL))
                .collect(Collectors.toList())
        );
    }

    @Override
    public Set<BenchmarkResult> findMatches(String text) {
        Set<BenchmarkResult> results = new HashSet<>();
        for (Pattern pattern : patterns) {
            for (MatchResult matchResult : RegexMatchFinder.find(text, pattern)) {
                results.add(convert(matchResult));
            }
        }
        return results;
    }
}
