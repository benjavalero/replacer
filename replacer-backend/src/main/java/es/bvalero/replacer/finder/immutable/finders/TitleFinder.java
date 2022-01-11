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
import java.util.Collection;
import java.util.List;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;
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
            findWordsInText(page.getTitle())
                .stream()
                .map(TitleFinder.TitleLinearFinder::new)
                .map(finder -> finder.find(page))
                .toArray(Iterable[]::new)
        );
    }

    private Collection<String> findWordsInText(String text) {
        final List<String> words = new ArrayList<>();
        int start = -1;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetter(text.charAt(i))) {
                if (start < 0) {
                    start = i;
                }
            } else {
                if (start >= 0) {
                    words.add(text.substring(start, i));
                    start = -1;
                }
            }
        }
        if (start >= 0) {
            words.add(text.substring(start));
        }
        return words;
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
            final String content = FinderUtils.toLowerCase(page.getContent());
            while (start >= 0 && start < content.length() && matches.isEmpty()) {
                start = findTitleWord(content, start, word, matches);
            }
            return matches.isEmpty() ? null : matches.get(0);
        }

        private int findTitleWord(String text, int start, String word, List<MatchResult> matches) {
            final String lowerCaseWord = FinderUtils.toLowerCase(word);
            final int wordStart = text.indexOf(lowerCaseWord, start);
            if (wordStart >= 0) {
                if (FinderUtils.isWordCompleteInText(wordStart, lowerCaseWord, text)) {
                    matches.add(LinearMatchResult.of(wordStart, word));
                }
                return wordStart + word.length();
            } else {
                return -1;
            }
        }
    }
}
