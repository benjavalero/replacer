package es.bvalero.replacer.finder.benchmark.surname;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Find all the person surnames in the text using one regex per word.
 * Then validate that the match is complete in the text and preceded by an uppercase word.
 * Con: iterates over all words independently, so the same position can be visited multiple times.
 */
class SurnameRegexFinder implements BenchmarkFinder {

    private final Collection<Pattern> words = new ArrayList<>();

    SurnameRegexFinder(Collection<String> words) {
        for (String word : words) {
            this.words.add(Pattern.compile(word));
        }
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return this.words.stream().flatMap(p -> RegexMatchFinder.find(page.getContent(), p));
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return (
            FinderUtils.isWordCompleteInText(match, page.getContent()) &&
            FinderUtils.isWordPrecededByUpperCase(match.start(), page.getContent())
        );
    }
}
