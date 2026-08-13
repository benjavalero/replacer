package es.bvalero.replacer.finder.benchmark.surname;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Find all the person surnames in the text using a single alternate regex.
 * Then validate that the match is complete in the text and preceded by an uppercase word.
 */
class SurnameRegexAlternateFinder implements BenchmarkFinder {

    private final Pattern words;

    SurnameRegexAlternateFinder(Collection<String> words) {
        final String alternations = '(' + FinderUtils.joinAlternate(words) + ')';
        this.words = Pattern.compile(alternations);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), this.words);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return (
            FinderUtils.isWordCompleteInText(match, page.getContent()) &&
            FinderUtils.isWordPrecededByUpperCase(match.start(), page.getContent())
        );
    }
}
