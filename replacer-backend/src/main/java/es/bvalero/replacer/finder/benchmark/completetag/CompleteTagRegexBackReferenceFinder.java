package es.bvalero.replacer.finder.benchmark.completetag;

import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;

class CompleteTagRegexBackReferenceFinder implements BenchmarkFinder {
    private Pattern pattern;

    CompleteTagRegexBackReferenceFinder(Set<String> tags) {
        String regex = String.format("<(%s)[^>/]*?>.+?</\\1>", StringUtils.join(tags, "|"));
        pattern = Pattern.compile(regex, Pattern.DOTALL);
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        return new HashSet<>(IterableUtils.toList(new RegexIterable<>(text, pattern, this::convert)));
    }
}
