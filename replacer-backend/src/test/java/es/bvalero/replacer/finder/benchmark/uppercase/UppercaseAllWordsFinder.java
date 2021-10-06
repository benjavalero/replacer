package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class UppercaseAllWordsFinder extends UppercaseBenchmarkFinder {

    private final Set<String> words = new HashSet<>();

    UppercaseAllWordsFinder(Collection<String> words) {
        this.words.addAll(words);
    }

    @Override
    public Set<BenchmarkResult> findMatches(String text) {
        Set<BenchmarkResult> matches = new HashSet<>();
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
                        if (
                            FinderUtils.startsWithUpperCase(word) &&
                            words.contains(word) &&
                            isWordPrecededByPunctuation(startWord, text)
                        ) {
                            matches.add(BenchmarkResult.of(startWord, word));
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
        return Character.isLetter(ch);
    }
}
