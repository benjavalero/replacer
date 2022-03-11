package es.bvalero.replacer.finder.benchmark.surname;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class SurnameIndexOfFinder implements BenchmarkFinder {

    private final Collection<String> words = new ArrayList<>();

    SurnameIndexOfFinder(Collection<String> words) {
        this.words.addAll(words);
    }

    @Override
    public Set<BenchmarkResult> findMatches(WikipediaPage page) {
        String text = page.getContent();
        // We loop over all the words and find them in the text with the indexOf function
        final Set<BenchmarkResult> matches = new HashSet<>();
        for (String word : this.words) {
            int start = 0;
            while (start >= 0) {
                start = text.indexOf(word, start);
                if (start >= 0) {
                    if (FinderUtils.isWordPrecededByUppercase(start, word, text)) {
                        matches.add(BenchmarkResult.of(start, word));
                    }
                    start++;
                }
            }
        }
        return matches;
    }
}
