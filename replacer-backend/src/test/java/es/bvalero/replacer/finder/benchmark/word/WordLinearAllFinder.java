package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.*;
import java.util.regex.MatchResult;

class WordLinearAllFinder implements BenchmarkFinder {

    private final Set<String> words = new HashSet<>();

    WordLinearAllFinder(Collection<String> words) {
        this.words.addAll(words);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return LinearMatchFinder.find(page, this::findMisspelling);
    }

    private int findMisspelling(WikipediaPage page, int start, List<MatchResult> matches) {
        final String text = page.getContent();
        final int startWord = findStartWord(text, start);
        if (startWord >= 0) {
            final int endWord = findEndWord(text, startWord);
            final String word = text.substring(startWord, endWord);
            if (words.contains(word) && FinderUtils.isWordCompleteInText(startWord, word, text)) {
                matches.add(LinearMatchResult.of(startWord, word));
            }
            return endWord;
        } else {
            return -1;
        }
    }

    private int findEndWord(String text, int startWord) {
        for (int j = startWord + 1; j < text.length(); j++) {
            if (!isLetter(text.charAt(j))) {
                return j;
            }
        }
        return text.length();
    }

    private int findStartWord(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (isLetter(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean isLetter(char ch) {
        return Character.isLetter(ch);
    }
}
