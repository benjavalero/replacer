package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinderPriority;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/** Find words in the page title */
@Component
class TitleFinder extends ImmutableCheckedFinder {

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.HIGH;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Immutable> find(FinderPage page) {
        // As the titles are not very long and therefore they have few words
        // the best approach is to iterate the list of words and find them in the text
        return IterableUtils.chainedIterable(
            Arrays
                .stream(page.getTitle().split("\\P{L}+"))
                .filter(StringUtils::isNotEmpty)
                .map(TitleFinder.TitleLinearFinder::new)
                .map(finder -> finder.find(page))
                .toArray(Iterable[]::new)
        );
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // We are overriding the more general find method
        throw new IllegalCallerException();
    }

    private static class TitleLinearFinder implements ImmutableFinder {

        private final String word;

        TitleLinearFinder(String word) {
            this.word = word;
        }

        @Override
        public Iterable<MatchResult> findMatchResults(FinderPage page) {
            return LinearMatchFinder.find(page, this::findResult);
        }

        @Nullable
        private MatchResult findResult(FinderPage page, int start) {
            final List<MatchResult> matches = new ArrayList<>();
            while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
                start = findTitleWord(page.getContent(), start, word, matches);
            }
            return matches.isEmpty() ? null : matches.get(0);
        }

        private int findTitleWord(String text, int start, String word, List<MatchResult> matches) {
            // Find the word case-sensitive improves the performance
            final int wordStart = text.indexOf(word, start);
            if (wordStart >= 0) {
                if (FinderUtils.isWordCompleteInText(wordStart, word, text)) {
                    matches.add(LinearMatchResult.of(wordStart, word));
                }
                return wordStart + word.length();
            } else {
                return -1;
            }
        }
    }
}