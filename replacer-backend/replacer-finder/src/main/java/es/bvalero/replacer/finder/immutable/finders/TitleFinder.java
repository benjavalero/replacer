package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.Arrays;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/** Find words in the page title */
@Component
class TitleFinder extends ImmutableCheckedFinder {

    private static final int MIN_WORD_LENGTH = 4;

    @Override
    public FinderPriority getPriority() {
        return FinderPriority.HIGH;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Immutable> find(FinderPage page) {
        // As the titles are not very long, and therefore they have few words,
        // the best approach is to iterate the list of words and find them in the text.
        return IterableUtils.chainedIterable(
            Arrays
                .stream(page.getTitle().split("\\P{L}+"))
                .filter(this::isTitleWordImmutable)
                .map(TitleLinearFinder::new)
                .map(finder -> finder.find(page))
                .toArray(Iterable[]::new)
        );
    }

    private boolean isTitleWordImmutable(@Nullable String word) {
        return word != null && word.length() >= MIN_WORD_LENGTH;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // We are overriding the more general find method
        throw new UnsupportedOperationException();
    }

    private static class TitleLinearFinder implements ImmutableFinder {

        private final String word;

        TitleLinearFinder(String word) {
            this.word = word;
        }

        @Override
        public Iterable<MatchResult> findMatchResults(FinderPage page) {
            return LinearMatchFinder.find(page, this::findTitleWord);
        }

        @Nullable
        private MatchResult findTitleWord(FinderPage page, int start) {
            final String text = page.getContent();
            if (start >= 0 && start < text.length()) {
                // Find the word case-sensitive improves the performance
                final int wordStart = text.indexOf(this.word, start);
                if (wordStart >= 0) {
                    return LinearMatchResult.of(wordStart, this.word);
                }
            }
            return null;
        }
    }
}