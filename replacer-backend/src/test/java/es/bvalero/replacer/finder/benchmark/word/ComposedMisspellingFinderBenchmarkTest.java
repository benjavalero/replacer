package es.bvalero.replacer.finder.benchmark.word;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.listing.ComposedMisspelling;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import es.bvalero.replacer.finder.replacement.finders.MisspellingComposedFinder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class ComposedMisspellingFinderBenchmarkTest extends BaseFinderBenchmark {

    @Test
    void testWordFinderBenchmark() throws ReplacerException {
        // Load the misspellings
        ComposedMisspellingLoader composedMisspellingLoader = new ComposedMisspellingLoader();
        ListingFinder listingFinder = new ListingOfflineFinder();
        composedMisspellingLoader.setComposedMisspellingParser(new ComposedMisspellingParser());
        Set<ComposedMisspelling> misspellings = composedMisspellingLoader.parseListing(
            listingFinder.getComposedMisspellingListing(WikipediaLanguage.getDefault())
        );

        // Extract the misspelling words
        MisspellingComposedFinder misspellingComposedFinder = new MisspellingComposedFinder();
        Map<String, Misspelling> misspellingMap = misspellingComposedFinder.buildMisspellingMap(
            misspellings.stream().map(cm -> (Misspelling) cm).collect(Collectors.toSet())
        );
        Set<String> words = misspellingMap.keySet();

        /* NOTE: We can use the same finders that we use for misspellings just with a different set of words */

        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();

        // Loop over all the words and find them in the text with a regex
        finders.add(new WordIndexOfFinder(words));
        finders.add(new WordRegexFinder(words));
        // finders.add(new WordAutomatonFinder(words)); // Medium
        // finders.add(new WordRegexCompleteFinder(words)); // Very long

        // Build an alternation with all the words and find the regex in the text
        // finders.add(new WordRegexAlternateFinder(words)); // Long
        finders.add(new WordAutomatonAlternateFinder(words));
        // finders.add(new WordRegexAlternateCompleteFinder(words)); // Medium

        // Find all words in the text and check if they are in the list
        // finders.add(new WordRegexAllFinder(words)); // Don't work with composed
        // finders.add(new WordAutomatonAllFinder(words)); // Don't work with composed
        // finders.add(new WordLinearAllFinder(words)); // Don't work with composed
        // finders.add(new WordRegexAllCompleteFinder(words)); // Don't work with composed

        runBenchmark(finders);

        MatcherAssert.assertThat(true, is(true));
    }
}
