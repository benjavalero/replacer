package es.bvalero.replacer.finder.benchmark.surname;

import es.bvalero.replacer.finder.BenchmarkResult;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class SurnameIndexOfFinder implements BenchmarkFinder {

    private final Collection<String> words = new ArrayList<>();

    SurnameIndexOfFinder(Collection<String> words) {
        this.words.addAll(words);
    }

    @Override
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        // We loop over all the words and find them in the text with the indexOf function
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        for (String word : this.words) {
            int start = 0;
            while (start >= 0) {
                start = text.indexOf(word, start);
                if (start >= 0) {
                    if (
                        FinderUtils.isWordCompleteInText(start, word, text) &&
                        FinderUtils.isWordPrecededByUpperCase(start, text)
                    ) {
                        matches.add(BenchmarkResult.of(start, word));
                    }
                    start += word.length();
                }
            }
        }
        return matches;
    }
}
