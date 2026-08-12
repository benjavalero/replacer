package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Loop over all the words/expressions and find them in the text with a regex.
 * The regex contains the word/expression surrounded by word boundaries.
 * No need to validate: the word boundaries already guarantee completeness.
 * Con: it iterates over all the words/expressions, so it may be slow for large collections.
 */
class WordRegexCompleteFinder implements BenchmarkFinder {

    private final List<Pattern> patterns;

    WordRegexCompleteFinder(Collection<String> words) {
        this.patterns = new ArrayList<>();
        for (String word : words) {
            this.patterns.add(Pattern.compile("\\b" + cleanWord(word) + "\\b", Pattern.UNICODE_CHARACTER_CLASS));
        }
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return patterns.stream().flatMap(p -> RegexMatchFinder.find(page.getContent(), p));
    }
    // No need to validate
}
