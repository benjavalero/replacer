package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderPage;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.TestOnly;

public interface BenchmarkFinder extends Finder<BenchmarkResult> {
    @RegExp
    String SEPARATOR_CLASS = "[\\p{L}\\d_]";

    @TestOnly
    default Set<BenchmarkResult> findMatches(String text) {
        return new HashSet<>(findList(text));
    }

    @Override
    default Iterable<MatchResult> findMatchResults(FinderPage page) {
        // As most benchmarks override the main method, this will not be called.
        throw new UnsupportedOperationException();
    }

    @Override
    default BenchmarkResult convert(MatchResult match, FinderPage page) {
        return BenchmarkResult.of(match.start(), match.group());
    }

    default String cleanWord(String word) {
        return word.replace("[", "\\[").replace("]", "\\]");
    }
}
