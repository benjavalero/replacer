package es.bvalero.replacer.finder.benchmark.word;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.misspelling.*;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaServiceOfflineImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class FalseFinderBenchmark extends BaseFinderBenchmark {

    @Test
    void testWordFinderBenchmark() throws Exception {
        WikipediaService wikipediaService = new WikipediaServiceOfflineImpl();
        String text = wikipediaService.getFalsePositiveListPageContent(WikipediaLanguage.SPANISH);

        // Load the false positives
        FalsePositiveManager falsePositiveManager = new FalsePositiveManager();
        Set<String> words = falsePositiveManager.parseItemsText(text);

        /* NOTE: We can use the same finders that we use for misspellings just with a different set of words */

        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new WordIndexOfFinder(words));
        // finders.add(new WordRegexFinder(words)); // Discarded: about 100 times slower
        // finders.add(new WordAutomatonFinder(words)); // Discarded: about 200 times slower
        // finders.add(new WordRegexCompleteFinder(words)); // Discarded: about 1000 times slower
        // finders.add(new WordRegexAlternateFinder(words)); // Discarded: about 500 times slower
        finders.add(new WordAutomatonAlternateFinder(words));
        // finders.add(new WordRegexAlternateCompleteFinder(words)); // Discarded: about 100 times slower
        // finders.add(new WordRegexAllFinder(words)); // Don't work with composed
        // finders.add(new WordAutomatonAllFinder(words)); // Don't work with composed
        // finders.add(new WordLinearAllFinder(words)); // Don't work with composed
        // finders.add(new WordRegexAllCompleteFinder(words)); // Don't work with composed

        runBenchmark(finders);

        MatcherAssert.assertThat(true, is(true));
    }
}
