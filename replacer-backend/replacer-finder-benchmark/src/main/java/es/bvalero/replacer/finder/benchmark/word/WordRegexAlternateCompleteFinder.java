package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Find all the words/expressions in the text with a regex.
 * The regex contains the alternation of all the words/expressions surrounded by word boundaries.
 * Then the result is checked to be complete in the text, e.g., in case of underscores.
 */
class WordRegexAlternateCompleteFinder implements BenchmarkFinder {

    private final Pattern pattern;

    WordRegexAlternateCompleteFinder(Collection<String> words) {
        String alternations = "\\b(" + FinderUtils.joinAlternate(words) + ")\\b";
        this.pattern = Pattern.compile(alternations);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), this.pattern);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }
}
