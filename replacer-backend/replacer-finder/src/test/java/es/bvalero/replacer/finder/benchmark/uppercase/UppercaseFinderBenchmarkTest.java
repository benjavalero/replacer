package es.bvalero.replacer.finder.benchmark.uppercase;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.immutable.finders.UppercaseFinder;
import es.bvalero.replacer.finder.listing.StandardMisspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.Test;

class UppercaseFinderBenchmarkTest extends BaseFinderBenchmark {

    private static final String fileName = "uppercase/uppercase-benchmark.csv";

    @Test
    void testBenchmark() throws ReplacerException {
        // Load the uppercase misspellings
        ListingFinder listingFinder = new ListingOfflineFinder();
        SimpleMisspellingLoader simpleMisspellingLoader = new SimpleMisspellingLoader(
            listingFinder,
            new SimpleMisspellingParser()
        );
        ComposedMisspellingLoader composedMisspellingLoader = new ComposedMisspellingLoader(
            listingFinder,
            new ComposedMisspellingParser()
        );
        SetValuedMap<WikipediaLanguage, StandardMisspelling> misspellings = new HashSetValuedHashMap<>();
        misspellings.putAll(
            WikipediaLanguage.getDefault(),
            simpleMisspellingLoader.parseListing(
                listingFinder.getSimpleMisspellingListing(WikipediaLanguage.getDefault())
            )
        );
        misspellings.putAll(
            WikipediaLanguage.getDefault(),
            composedMisspellingLoader.parseListing(
                listingFinder.getComposedMisspellingListing(WikipediaLanguage.getDefault())
            )
        );

        // Extract the uppercase words
        UppercaseFinder uppercaseFinder = new UppercaseFinder(simpleMisspellingLoader, composedMisspellingLoader);
        Set<String> words = uppercaseFinder.getUppercaseWords(misspellings).get(WikipediaLanguage.getDefault());

        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new UppercaseIndexOfFinder(words));
        finders.add(new UppercaseAllWordsFinder(words));
        // finders.add(new UppercaseRegexIterateFinder(words)); // Long
        // finders.add(new UppercaseAutomatonIterateFinder(words)); // Medium
        // finders.add(new UppercaseRegexLookBehindFinder(words)); // Very long
        // finders.add(new UppercaseRegexAlternateFinder(words)); // Short
        finders.add(new UppercaseAutomatonAlternateFinder(words));
        // finders.add(new UppercaseRegexAlternateLookBehindFinder(words)); // Medium
        finders.add(new UppercaseAutomatonAlternateAllFinder(words));
        finders.add(new UppercaseAhoCorasickWholeFinder(words));
        finders.add(new UppercaseAhoCorasickWholeLongestFinder(words));

        List<Finder<?>> benchmarkFinders = new ArrayList<>(finders);
        runBenchmark(benchmarkFinders, fileName);

        assertTrue(true);
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        generateBoxplot(fileName, "Uppercase");
    }
}
