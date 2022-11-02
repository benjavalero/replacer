package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.Collection;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.lang.Nullable;

class UppercaseIndexOfFinder extends UppercaseBenchmarkFinder {

    private final Collection<String> words;

    UppercaseIndexOfFinder(Collection<String> words) {
        this.words = words;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        // Only works converting to list the iterables in the loop
        return IterableUtils.chainedIterable(
            words.stream().map(UppercaseLinearFinder::new).map(finder -> finder.find(page)).toArray(Iterable[]::new)
        );
    }

    private static class UppercaseLinearFinder {

        private final String uppercase;

        UppercaseLinearFinder(String word) {
            this.uppercase = word;
        }

        public Iterable<MatchResult> find(WikipediaPage page) {
            return IterableUtils.toList(LinearMatchFinder.find(page, this::findUppercase));
        }

        @Nullable
        private MatchResult findUppercase(WikipediaPage page, int start) {
            final String text = page.getContent();
            while (start >= 0 && start < text.length()) {
                final int startUppercase = findStartUppercase(text, start);
                if (startUppercase >= 0) {
                    if (
                        FinderUtils.isWordCompleteInText(startUppercase, uppercase, text) &&
                        UppercaseBenchmarkFinder.isWordPrecededByPunctuation(startUppercase, text)
                    ) {
                        return LinearMatchResult.of(startUppercase, uppercase);
                    } else {
                        // The char after the word is a non-letter, so we can start searching the next word one position after.
                        start = startUppercase + uppercase.length() + 1;
                    }
                } else {
                    return null;
                }
            }
            return null;
        }

        private int findStartUppercase(String text, int start) {
            return text.indexOf(uppercase, start);
        }
    }
}
