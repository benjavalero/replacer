package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Find all the words in the text surrounded by word boundaries or underscores.
 * Therefore, we can only use it for simple misspellings.
 * Then check if it is a wanted word.
 * Then there is no need to check if the result is complete in the text.
 */
class WordRegexAllCompleteSeparatorsFinder implements BenchmarkFinder {

    private final Pattern wordPattern;
    private final Set<String> words = new HashSet<>();

    WordRegexAllCompleteSeparatorsFinder(Collection<String> words) {
        final String leftSeparator = "(?<!%s)".formatted(SEPARATOR_CLASS);
        final String rightSeparator = "(?!%s)".formatted(SEPARATOR_CLASS);
        final String wordRegex = "\\p{L}++";
        final String regex = leftSeparator + wordRegex + rightSeparator;
        this.wordPattern = Pattern.compile(regex);
        this.words.addAll(words);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), wordPattern);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return this.words.contains(match.group());
    }
}
