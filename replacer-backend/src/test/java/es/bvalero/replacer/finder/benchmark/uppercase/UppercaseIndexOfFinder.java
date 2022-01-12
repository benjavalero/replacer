package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class UppercaseIndexOfFinder extends UppercaseBenchmarkFinder {

    private final Collection<String> words;

    UppercaseIndexOfFinder(Collection<String> words) {
        this.words = words;
    }

    @Override
    public Set<BenchmarkResult> findMatches(FinderPage page) {
        String text = page.getContent();
        // We loop over all the words and find them in the text with the indexOf function
        Set<BenchmarkResult> matches = new HashSet<>();
        for (String word : this.words) {
            int start = 0;
            while (start >= 0) {
                start = text.indexOf(word, start);
                if (start >= 0) {
                    if (
                        FinderUtils.isWordCompleteInText(start, word, text) && isWordPrecededByPunctuation(start, text)
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
