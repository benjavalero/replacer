package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class UppercaseIndexOfFinder implements BenchmarkFinder {
    private static final Collection<Character> PUNCTUATIONS = Arrays.asList('!', '#', '*', '|', '=', '.');

    private final Collection<String> words;

    UppercaseIndexOfFinder(Collection<String> words) {
        this.words = words;
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        // We loop over all the words and find them in the text with the indexOf function
        Set<FinderResult> matches = new HashSet<>();
        for (String word : this.words) {
            int start = 0;
            while (start >= 0) {
                start = text.indexOf(word, start);
                if (start >= 0) {
                    if (isWordPrecededByPunctuation(start, text)) {
                        matches.add(FinderResult.of(start, word));
                    }
                    start++;
                }
            }
        }
        return matches;
    }

    private boolean isWordPrecededByPunctuation(int start, String text) {
        // Find the last character not space before the word
        int pos = start - 1;
        while (pos >= 0) {
            if (!Character.isWhitespace(text.charAt(pos))) {
                break;
            }
            pos--;
        }
        return PUNCTUATIONS.contains(text.charAt(pos));
    }
}
