package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.*;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;

class WordLinearAllFinder implements BenchmarkFinder {

    private final Set<String> words = new HashSet<>();

    WordLinearAllFinder(Collection<String> words) {
        this.words.addAll(words);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return LinearMatchFinder.find(page, this::findMisspelling);
    }

    @Nullable
    private MatchResult findMisspelling(WikipediaPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final int startWord = findStartWord(text, start);
            if (startWord >= 0) {
                final int endWord = findEndWord(text, startWord);
                final String word = text.substring(startWord, endWord);
                // The word is wrapped by non-letters, so we still need to validate the separators.
                if (words.contains(word) && FinderUtils.isWordCompleteInText(startWord, word, text)) {
                    return LinearMatchResult.of(startWord, word);
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
