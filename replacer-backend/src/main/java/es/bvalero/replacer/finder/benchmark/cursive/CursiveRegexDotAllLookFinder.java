package es.bvalero.replacer.finder.benchmark.cursive;

import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.collections4.IterableUtils;

class CursiveRegexDotAllLookFinder implements BenchmarkFinder {
    private static final String TWO_QUOTES_ONLY = "(?<!')''(?!')";
    private static final String CURSIVE_REGEX = "%s.*?(%s|\n)";
    private static final Pattern CURSIVE_PATTERN = Pattern.compile(
        String.format(CURSIVE_REGEX, TWO_QUOTES_ONLY, TWO_QUOTES_ONLY)
    );

    public Set<FinderResult> findMatches(String text) {
        return new HashSet<>(IterableUtils.toList(new RegexIterable<>(text, CURSIVE_PATTERN, this::convert)));
    }
}
