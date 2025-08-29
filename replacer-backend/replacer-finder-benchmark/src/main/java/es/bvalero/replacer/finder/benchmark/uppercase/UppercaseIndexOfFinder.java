package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.springframework.lang.Nullable;

class UppercaseIndexOfFinder extends UppercaseBenchmarkFinder {

    private final Collection<String> words;

    UppercaseIndexOfFinder(Collection<String> words) {
        this.words = words;
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return words.stream().map(UppercaseLinearFinder::new).flatMap(finder -> finder.find(page));
    }

    private static class UppercaseLinearFinder {

        private final String uppercase;

        UppercaseLinearFinder(String word) {
            this.uppercase = word;
        }

        private Stream<MatchResult> find(FinderPage page) {
            return LinearMatchFinder.find(page, this::findUppercase);
        }

        @Nullable
        private MatchResult findUppercase(FinderPage page, int start) {
            final String text = page.getContent();
            while (start >= 0 && start < text.length()) {
                final int startUppercase = findStartUppercase(text, start);
                if (startUppercase >= 0) {
                    if (
                        FinderUtils.isWordCompleteInText(startUppercase, uppercase, text) &&
                        UppercaseBenchmarkFinder.isWordPrecededByPunctuation(startUppercase, text)
                    ) {
                        return FinderMatchResult.of(startUppercase, uppercase);
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
