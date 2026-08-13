package es.bvalero.replacer.finder.benchmark.person;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Find all the person names in the text using one regex per word.
 * Then validate that the match is followed by an uppercase word.
 * Con: iterates over all words independently, so the same position can be visited multiple times.
 */
class PersonRegexFinder implements BenchmarkFinder {

    private final List<Pattern> words;

    PersonRegexFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(Pattern.compile(word));
        }
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        // We loop over all the words and find them in the text with a regex
        return this.words.stream().flatMap(p -> RegexMatchFinder.find(page.getContent(), p));
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordFollowedByUpperCase(match.start(), match.group(), page.getContent());
    }
}
