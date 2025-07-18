package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.Collection;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.lang.Nullable;

/**
 * Loop over all the words/expressions and find them in the text using a simple "indexOf".
 * Then the result is checked to be complete in the text.
 */
class WordLinearFinder implements BenchmarkFinder {

    private final Collection<String> words;

    WordLinearFinder(Collection<String> words) {
        this.words = words;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // Only works converting to list the iterables in the loop
        return IterableUtils.chainedIterable(
            words.stream().map(MisspellingLinearFinder::new).map(finder -> finder.find(page)).toArray(Iterable[]::new)
        );
    }

    private static class MisspellingLinearFinder {

        private final String misspelling;

        MisspellingLinearFinder(String word) {
            this.misspelling = word;
        }

        public Iterable<MatchResult> find(FinderPage page) {
            return IterableUtils.toList(LinearMatchFinder.find(page, this::findMisspelling));
        }

        @Nullable
        private MatchResult findMisspelling(FinderPage page, int start) {
            final String text = page.getContent();
            while (start >= 0 && start < text.length()) {
                final int startMisspelling = findStartMisspelling(text, start);
                if (startMisspelling >= 0) {
                    if (FinderUtils.isWordCompleteInText(startMisspelling, misspelling, text)) {
                        return FinderMatchResult.of(startMisspelling, misspelling);
                    } else {
                        // The char after the word is a non-letter, so we can start searching for the next word one position after.
                        start = startMisspelling + misspelling.length() + 1;
                    }
                } else {
                    return null;
                }
            }
            return null;
        }

        private int findStartMisspelling(String text, int start) {
            return text.indexOf(misspelling, start);
        }
    }
}
