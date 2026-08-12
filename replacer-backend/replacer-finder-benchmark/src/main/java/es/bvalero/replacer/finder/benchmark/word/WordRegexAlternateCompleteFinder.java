package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Collection;
import java.util.Comparator;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Find all the words/expressions in the text with a regex.
 * The regex contains the alternation of all the words/expressions surrounded by word boundaries.
 * No need to validate: the word boundaries already guarantee completeness.
 */
class WordRegexAlternateCompleteFinder implements BenchmarkFinder {

    private final Pattern pattern;

    WordRegexAlternateCompleteFinder(Collection<String> words) {
        Iterable<String> cleanWords = words.stream().sorted(Comparator.reverseOrder()).map(this::cleanWord).toList();
        String alternations = "\\b(" + FinderUtils.joinAlternate(cleanWords) + ")\\b";
        this.pattern = Pattern.compile(alternations, Pattern.UNICODE_CHARACTER_CLASS);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), this.pattern);
    }
    // No need to validate
}
