package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Loop over all the words/expressions and find them in the text with a regex.
 * Then the result is checked to be complete in the text.
 * Con: it iterates over all the words/expressions, so it may be slow for large collections.
 */
class WordRegexFinder implements BenchmarkFinder {

    private final List<Pattern> patterns;

    WordRegexFinder(Collection<String> words) {
        this.patterns = new ArrayList<>();
        for (String word : words) {
            this.patterns.add(Pattern.compile(cleanWord(word)));
        }
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return patterns.stream().flatMap(p -> RegexMatchFinder.find(page.getContent(), p));
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordCompleteInText(match, page.getContent());
    }
}
