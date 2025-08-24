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
 * The regex contains the alternation of all the words/expressions.
 * Then the result is checked to be complete in the text.
 */
class WordRegexAlternateFinder implements BenchmarkFinder {

    private final Pattern pattern;

    WordRegexAlternateFinder(Collection<String> words) {
        Iterable<String> cleanWords = words.stream().sorted(Comparator.reverseOrder()).map(this::cleanWord).toList();
        String alternations = FinderUtils.joinAlternate(cleanWords);
        this.pattern = Pattern.compile(alternations);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), this.pattern);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }
}
