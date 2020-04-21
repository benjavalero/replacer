package es.bvalero.replacer.finder.benchmark;

import java.util.Set;
import java.util.regex.MatchResult;

public interface BenchmarkFinder {
    Set<FinderResult> findMatches(String text);

    default FinderResult convert(MatchResult match) {
        return FinderResult.of(match.start(), match.group());
    }
}
