package es.bvalero.replacer.finder.benchmark.word;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.listing.FalsePositiveManager;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class FalseFinderBenchmarkTest extends BaseFinderBenchmark {

    @Test
    void testWordFinderBenchmark() throws ReplacerException {
        // Load the false positives
        FalsePositiveManager falsePositiveManager = new FalsePositiveManager();
        falsePositiveManager.setListingFinder(new ListingOfflineFinder());
        falsePositiveManager.scheduledItemListUpdate();
        Set<String> words = falsePositiveManager.getItems(WikipediaLanguage.getDefault());

        /* NOTE: We can use the same finders that we use for misspellings just with a different set of words */

        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new WordIndexOfFinder(words));
        finders.add(new WordRegexFinder(words));
        finders.add(new WordAutomatonFinder(words));
        // finders.add(new WordRegexCompleteFinder(words)); // Very long
        // finders.add(new WordRegexAlternateFinder(words)); // Long
        finders.add(new WordAutomatonAlternateFinder(words));
        finders.add(new WordRegexAlternateCompleteFinder(words));
        // finders.add(new WordRegexAllFinder(words)); // Don't work with composed
        // finders.add(new WordAutomatonAllFinder(words)); // Don't work with composed
        // finders.add(new WordLinearAllFinder(words)); // Don't work with composed
        // finders.add(new WordRegexAllCompleteFinder(words)); // Don't work with composed

        runBenchmark(finders, WARM_UP / 10, ITERATIONS / 10);

        MatcherAssert.assertThat(true, is(true));
    }
}
