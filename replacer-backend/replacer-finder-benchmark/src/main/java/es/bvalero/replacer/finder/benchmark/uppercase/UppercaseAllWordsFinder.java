package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.springframework.lang.Nullable;

class UppercaseAllWordsFinder extends UppercaseBenchmarkFinder {

    private final Set<String> words = new HashSet<>();

    UppercaseAllWordsFinder(Collection<String> words) {
        this.words.addAll(words);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findUppercase);
    }

    @Nullable
    private MatchResult findUppercase(FinderPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final int startWord = findStartWord(text, start);
            if (startWord >= 0) {
                final int endWord = findEndWord(text, startWord);
                final String word = text.substring(startWord, endWord);
                // The word is wrapped by non-letters, so we still need to validate the separators.
                if (
                    FinderUtils.startsWithUpperCase(word) &&
                    words.contains(word) &&
                    FinderUtils.isWordCompleteInText(startWord, word, text) &&
                    isWordPrecededByPunctuation(startWord, text)
                ) {
                    return FinderMatchResult.of(startWord, word);
                } else {
                    // The char after the word is a non-letter, so we can start searching the next word one position after.
                    start = endWord + 1;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    private int findStartWord(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (isLetter(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private int findEndWord(String text, int startWord) {
        for (int i = startWord + 1; i < text.length(); i++) {
            if (!isLetter(text.charAt(i))) {
                return i;
            }
        }
        return text.length();
    }

    private boolean isLetter(char ch) {
        return Character.isLetter(ch);
    }
}
