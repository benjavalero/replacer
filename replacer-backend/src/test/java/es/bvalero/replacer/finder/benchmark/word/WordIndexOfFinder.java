package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class WordIndexOfFinder implements BenchmarkFinder {

    private final Collection<String> words;

    WordIndexOfFinder(Collection<String> words) {
        this.words = words;
    }

    @Override
    public Set<BenchmarkResult> findMatches(WikipediaPage page) {
        String text = page.getContent();
        // We loop over all the words and find them in the text with the indexOf function
        Set<BenchmarkResult> matches = new HashSet<>();
        for (String word : this.words) {
            int start = 0;
            while (start >= 0) {
                start = text.indexOf(word, start);
                if (start >= 0) {
                    if (FinderUtils.isWordCompleteInText(start, word, text)) {
                        matches.add(BenchmarkResult.of(start, word));
                    }
                    start++;
                }
            }
        }
        return matches;
    }
}
