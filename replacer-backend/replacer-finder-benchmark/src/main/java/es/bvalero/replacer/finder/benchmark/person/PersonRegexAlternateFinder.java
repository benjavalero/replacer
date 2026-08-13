package es.bvalero.replacer.finder.benchmark.person;

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
 * Find all the person names in the text using a single alternate regex.
 * Then validate that the match is followed by an uppercase word.
 */
class PersonRegexAlternateFinder implements BenchmarkFinder {

    private final Pattern words;

    PersonRegexAlternateFinder(Collection<String> words) {
        String alternations = '(' + FinderUtils.joinAlternate(words) + ')';
        this.words = Pattern.compile(alternations);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), this.words);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordFollowedByUpperCase(match.start(), match.group(), page.getContent());
    }
}
