package es.bvalero.replacer.finder.benchmark.uppercase;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.listing.MisspellingManager;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class UppercaseFinderBenchmarkTest extends BaseFinderBenchmark {

    @Test
    void testBenchmark() throws ReplacerException {
        // Load the uppercase misspellings
        MisspellingManager misspellingManager = new MisspellingManager();
        misspellingManager.setListingFinder(new ListingOfflineFinder());
        misspellingManager.scheduledItemListUpdate();
        Set<String> words = misspellingManager.getUppercaseWords(WikipediaLanguage.getDefault());

        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new UppercaseIndexOfFinder(words));
        // finders.add(new UppercaseRegexIterateFinder(words)); // Long
        // finders.add(new UppercaseAutomatonIterateFinder(words)); // Medium
        // finders.add(new UppercaseRegexLookBehindFinder(words)); // Very long
        finders.add(new UppercaseRegexAlternateFinder(words));
        finders.add(new UppercaseAutomatonAlternateFinder(words));
        finders.add(new UppercaseRegexAlternateLookBehindFinder(words));

        runBenchmark(finders);

        MatcherAssert.assertThat(true, is(true));
    }
}
