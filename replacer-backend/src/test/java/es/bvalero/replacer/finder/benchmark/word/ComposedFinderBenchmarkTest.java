package es.bvalero.replacer.finder.benchmark.word;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class ComposedFinderBenchmarkTest extends BaseFinderBenchmark {

    @Test
    void testWordFinderBenchmark() throws ReplacerException {
        // Load the misspellings
        Set<String> words = ReplacementFinderService.getComposedMisspellings();

        /* NOTE: We can use the same finders that we use for misspellings just with a different set of words */

        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();

        // Loop over all the words and find them in the text with a regex
        finders.add(new WordIndexOfFinder(words));
        finders.add(new WordRegexFinder(words));
        finders.add(new WordAutomatonFinder(words));
        // finders.add(new WordRegexCompleteFinder(words)); // Discarded: about 700 times slower

        // Build an alternation with all the words and find the regex in the text
        finders.add(new WordRegexAlternateFinder(words));
        finders.add(new WordAutomatonAlternateFinder(words)); // Winner
        finders.add(new WordRegexAlternateCompleteFinder(words));

        // Find all words in the text and check if they are in the list
        // finders.add(new WordRegexAllFinder(words)); // Don't work with composed
        // finders.add(new WordAutomatonAllFinder(words)); // Don't work with composed
        // finders.add(new WordLinearAllFinder(words)); // Don't work with composed
        // finders.add(new WordRegexAllCompleteFinder(words)); // Don't work with composed

        runBenchmark(finders, 5, 50);

        MatcherAssert.assertThat(true, is(true));
    }
}
