package es.bvalero.replacer.finder.benchmark.person;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class PersonIndexOfFinder implements BenchmarkFinder {

    private final Collection<String> words;

    PersonIndexOfFinder(Collection<String> words) {
        this.words = words;
    }

    @Override
    public Iterable<BenchmarkResult> find(WikipediaPage page) {
        final String text = page.getContent();
        // We loop over all the words and find them in the text with the indexOf function
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        for (String word : this.words) {
            int start = 0;
            while (start >= 0) {
                start = text.indexOf(word, start);
                if (start >= 0) {
                    if (FinderUtils.isWordFollowedByUpperCase(start, word, text)) {
                        matches.add(BenchmarkResult.of(start, word));
                    }
                    start += word.length();
                }
            }
        }
        return matches;
    }
}
