package es.bvalero.replacer.finder.benchmark.surname;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * Find all the person surnames in the text using indexOf for each word via {@link LinearMatchFinder}.
 * Then validate that the match is complete in the text and preceded by an uppercase word.
 * Con: iterates over all words independently, so the same position can be visited multiple times.
 */
class SurnameIndexOfFinder implements BenchmarkFinder {

    private final Collection<String> words = new ArrayList<>();

    SurnameIndexOfFinder(Collection<String> words) {
        this.words.addAll(words);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return words.stream().flatMap(word -> LinearMatchFinder.find(page, (p, start) -> findWord(p, start, word)));
    }

    @Nullable
    private MatchResult findWord(FinderPage page, int start, String word) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final int startWord = text.indexOf(word, start);
            if (startWord >= 0) {
                if (FinderUtils.isWordCompleteInText(startWord, word, text)) {
                    return FinderMatchResult.of(startWord, word);
                } else {
                    start = startWord + word.length() + 1;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordPrecededByUpperCase(match.start(), page.getContent());
    }
}
