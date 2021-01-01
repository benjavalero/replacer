package es.bvalero.replacer.finder.benchmark.surname;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class SurnameIndexOfFinder implements BenchmarkFinder {
    private final Collection<String> words;

    SurnameIndexOfFinder(Collection<String> words) {
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
                    if (FinderUtils.isWordPrecededByUppercase(start, word, text)) {
                        matches.add(FinderResult.of(start, word));
                    }
                    start++;
                }
            }
        }
        return matches;
    }
}
