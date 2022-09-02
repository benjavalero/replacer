package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.Finder;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import org.jetbrains.annotations.TestOnly;

public interface BenchmarkFinder extends Finder<BenchmarkResult> {
    @TestOnly
    default Set<BenchmarkResult> findMatches(String text) {
        return new HashSet<>(findList(text));
    }

    @Override
    default Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        // As most benchmarks override the main method this will not be called
        throw new IllegalCallerException();
    }

    @Override
    default BenchmarkResult convert(MatchResult match, WikipediaPage page) {
        return convert(match);
    }

    default BenchmarkResult convert(MatchResult match) {
        return BenchmarkResult.of(match.start(), match.group());
    }
}
