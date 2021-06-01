package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.jetbrains.annotations.TestOnly;

public interface BenchmarkFinder extends Finder<BenchmarkResult> {
    @TestOnly
    default Set<BenchmarkResult> findMatches(String text) {
        return new HashSet<>(this.findList(text));
    }

    @Override
    default Iterable<MatchResult> findMatchResults(FinderPage page) {
        // As most benchmarks override the main method this will not be called
        throw new IllegalCallerException();
    }

    @Override
    default BenchmarkResult convert(MatchResult match) {
        return BenchmarkResult.of(match.start(), match.group());
    }

    /* UTILS */

    default List<String> toUpperCase(List<String> names) {
        return names.stream().map(this::toUpperCase).collect(Collectors.toList());
    }

    private String toUpperCase(String word) {
        return FinderUtils.startsWithLowerCase(word) ? FinderUtils.setFirstUpperCaseClass(word) : word;
    }
}
