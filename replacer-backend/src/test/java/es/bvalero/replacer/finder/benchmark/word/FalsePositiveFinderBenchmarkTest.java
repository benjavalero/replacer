package es.bvalero.replacer.finder.benchmark.word;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.listing.FalsePositive;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class FalsePositiveFinderBenchmarkTest extends BaseFinderBenchmark {

    @Test
    void testWordFinderBenchmark() throws ReplacerException {
        // Load the false positives
        FalsePositiveLoader falsePositiveLoader = new FalsePositiveLoader();
        ListingFinder listingFinder = new ListingOfflineFinder();
        falsePositiveLoader.setFalsePositiveParser(new FalsePositiveParser());
        Set<FalsePositive> falsePositives = falsePositiveLoader.parseListing(
            listingFinder.getFalsePositiveListing(WikipediaLanguage.getDefault())
        );

        // Extract the false positive expressions
        Set<String> words = falsePositives.stream().map(FalsePositive::getExpression).collect(Collectors.toSet());

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

        assertTrue(true);
    }
}
