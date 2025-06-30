package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Find all the words in the text surrounded by word boundaries.
 * Therefore, we can only use it for simple misspellings.
 * Then check if it is a wanted word.
 * Then check if it is complete in the text, e.g., in case of underscores.
 */
class WordRegexAllCompleteFinder implements BenchmarkFinder {

    private final Pattern wordPattern;
    private final Set<String> words = new HashSet<>();

    WordRegexAllCompleteFinder(Collection<String> words) {
        this.wordPattern = Pattern.compile("\\b\\p{L}+\\b");
        this.words.addAll(words);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), wordPattern);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        final String word = match.group();
        return this.words.contains(word) && FinderUtils.isWordCompleteInText(match.start(), word, page.getContent());
    }
}
