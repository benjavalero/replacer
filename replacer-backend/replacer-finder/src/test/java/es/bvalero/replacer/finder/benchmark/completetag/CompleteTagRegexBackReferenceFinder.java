package es.bvalero.replacer.finder.benchmark.completetag;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

class CompleteTagRegexBackReferenceFinder implements BenchmarkFinder {

    private final Pattern pattern;

    CompleteTagRegexBackReferenceFinder(Set<String> tags) {
        String regex = String.format("<(%s)[^>/]*?>.+?</\\1>", FinderUtils.joinAlternate(tags));
        pattern = Pattern.compile(regex, Pattern.DOTALL);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), pattern);
    }
}
