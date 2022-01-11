package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderPage;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.TestOnly;

public interface BenchmarkFinder extends Finder<BenchmarkResult> {
    @TestOnly
    default Set<BenchmarkResult> findMatches(String text) {
        // Only transform the iterable without validating not to penalize the performance of the benchmark
        return new HashSet<>(IterableUtils.toList(this.find(FinderPage.of(text))));
    }

    @TestOnly
    default Set<BenchmarkResult> findMatches(FinderPage page) {
        // Only transform the iterable without validating not to penalize the performance of the benchmark
        return new HashSet<>(IterableUtils.toList(this.find(page)));
    }

    @Override
    default Iterable<MatchResult> findMatchResults(FinderPage page) {
        // As most benchmarks override the main method this will not be called
        throw new IllegalCallerException();
    }

    @Override
    default BenchmarkResult convert(MatchResult match, FinderPage page) {
        return convert(match);
    }

    default BenchmarkResult convert(MatchResult match) {
        return BenchmarkResult.of(match.start(), match.group());
    }
}
