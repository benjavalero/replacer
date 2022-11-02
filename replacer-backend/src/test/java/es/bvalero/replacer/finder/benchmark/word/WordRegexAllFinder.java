package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

class WordRegexAllFinder implements BenchmarkFinder {

    private final Pattern wordPattern;
    private final Set<String> words = new HashSet<>();

    WordRegexAllFinder(Collection<String> words) {
        this.wordPattern = Pattern.compile("\\p{L}+");
        this.words.addAll(words);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return RegexMatchFinder.find(page.getContent(), wordPattern);
    }

    @Override
    public boolean validate(MatchResult match, WikipediaPage page) {
        final String word = match.group();
        // The word is wrapped by non-letters, so we still need to validate the separators.
        return this.words.contains(word) && FinderUtils.isWordCompleteInText(match.start(), word, page.getContent());
    }
}
