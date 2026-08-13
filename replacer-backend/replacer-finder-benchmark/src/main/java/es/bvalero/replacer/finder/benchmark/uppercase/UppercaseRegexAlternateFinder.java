package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Build a single alternate regex with all the words and find them in the text.
 * Then validate that the match is preceded by the expected punctuation.
 * Con: the alternate regex can be slow for large collections.
 */
class UppercaseRegexAlternateFinder extends UppercaseBenchmarkFinder {

    private final Pattern pattern;

    UppercaseRegexAlternateFinder(Collection<String> words) {
        this.pattern = Pattern.compile(String.format("(?:%s)", FinderUtils.joinAlternate(words)));
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), pattern);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return isWordPrecededByPunctuation(match.start(), page.getContent());
    }
}
