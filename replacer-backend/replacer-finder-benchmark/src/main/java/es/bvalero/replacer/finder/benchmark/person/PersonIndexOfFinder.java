package es.bvalero.replacer.finder.benchmark.person;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * Find all the person names in the text using indexOf for each word via {@link LinearMatchFinder}.
 * Then validate that the match is followed by an uppercase word.
 * Con: iterates over all words independently, so the same position can be visited multiple times.
 */
class PersonIndexOfFinder implements BenchmarkFinder {

    private final Collection<String> words;

    PersonIndexOfFinder(Collection<String> words) {
        this.words = words;
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

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordFollowedByUpperCase(match.start(), match.group(), page.getContent());
    }
}
