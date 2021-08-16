package es.bvalero.replacer.finder.benchmark.uppercase;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.immutable.finders.UppercaseAfterFinder;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class UppercaseFinderBenchmarkTest extends BaseFinderBenchmark {

    @Test
    void testBenchmark() throws ReplacerException {
        // Load the uppercase misspellings
        SimpleMisspellingLoader simpleMisspellingLoader = new SimpleMisspellingLoader();
        ListingFinder listingFinder = new ListingOfflineFinder();
        simpleMisspellingLoader.setSimpleMisspellingParser(new SimpleMisspellingParser());
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> misspellings = new HashSetValuedHashMap<>();
        misspellings.putAll(
            WikipediaLanguage.getDefault(),
            simpleMisspellingLoader.parseListing(
                listingFinder.getSimpleMisspellingListing(WikipediaLanguage.getDefault())
            )
        );

        // Extract the uppercase words
        UppercaseAfterFinder uppercaseAfterFinder = new UppercaseAfterFinder();
        Set<String> words = uppercaseAfterFinder.getUppercaseWords(misspellings).get(WikipediaLanguage.getDefault());

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
