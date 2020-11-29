package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class WordLinearAllFinder implements BenchmarkFinder {
    private final Set<String> words = new HashSet<>();

    WordLinearAllFinder(Collection<String> words) {
        this.words.addAll(words);
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        int start = 0;
        while (start >= 0) {
            // Find next letter
            int startWord = -1;
            for (int i = start; i < text.length(); i++) {
                if (isLetter(text.charAt(i))) {
                    startWord = i;
                    break;
                }
            }

            if (startWord >= 0) {
                // Find complete word
                for (int j = startWord + 1; j < text.length(); j++) {
                    if (!isLetter(text.charAt(j))) {
                        String word = text.substring(startWord, j);
                        if (words.contains(word)) {
                            matches.add(FinderResult.of(startWord, word));
                        }
                        start = j;
                        break;
                    }
                }
            } else {
                start = -1;
            }
        }
        return matches;
    }

    private boolean isLetter(char ch) {
        return Character.isLetter(ch) || ch == '-' || ch == '\'';
    }
}
