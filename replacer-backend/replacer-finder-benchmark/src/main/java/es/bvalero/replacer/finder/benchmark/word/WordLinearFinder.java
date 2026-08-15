package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/**
 * Loop over all the words/expressions and find them in the text using a simple "indexOf".
 * Then the result is checked to be complete in the text.
 * Con: it iterates over all the words/expressions, so it may be slow for large collections.
 */
class WordLinearFinder implements BenchmarkFinder {

    private final Collection<String> words;

    WordLinearFinder(Collection<String> words) {
        this.words = words;
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return words.stream().flatMap(word -> LinearMatchFinder.find(page, (p, start) -> findWord(p, start, word)));
    }

    private MatchResult findWord(FinderPage page, int start, String word) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final int startWord = text.indexOf(word, start);
            if (startWord >= 0) {
                if (FinderUtils.isWordCompleteInText(startWord, word, text)) {
                    return FinderMatchRange.of(text, startWord, word);
                } else {
                    start = startWord + word.length() + 1;
                }
            } else {
                return null;
            }
        }
        return null;
    }
}
