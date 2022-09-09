package es.bvalero.replacer.finder.benchmark.word;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import es.bvalero.replacer.finder.replacement.finders.MisspellingSimpleFinder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class SimpleMisspellingFinderBenchmarkTest extends BaseFinderBenchmark {

    private static final String fileName = "word/simple-misspelling-benchmark.csv";

    @Test
    void testWordFinderBenchmark() throws ReplacerException {
        // Load the misspellings
        SimpleMisspellingLoader simpleMisspellingLoader = new SimpleMisspellingLoader();
        ListingFinder listingFinder = new ListingOfflineFinder();
        simpleMisspellingLoader.setSimpleMisspellingParser(new SimpleMisspellingParser());
        Set<SimpleMisspelling> misspellings = simpleMisspellingLoader.parseListing(
            listingFinder.getSimpleMisspellingListing(WikipediaLanguage.getDefault())
        );

        // Extract the misspelling words
        MisspellingSimpleFinder misspellingSimpleFinder = new MisspellingSimpleFinder();
        Map<String, Misspelling> misspellingMap = misspellingSimpleFinder.buildMisspellingMap(
            misspellings.stream().map(sm -> (Misspelling) sm).collect(Collectors.toSet())
        );
        Set<String> words = misspellingMap.keySet();

        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();

        // Loop over all the misspelling words and find them in the text with a regex
        // finders.add(new WordLinearFinder(words)); // Short
        // finders.add(new WordRegexFinder(words)); // Medium
        // finders.add(new WordAutomatonFinder(words)); // Discarded: we need to increase too much the heap size
        // finders.add(new WordRegexCompleteFinder(words)); // Very long
        // finders.add(new WordRegexCompleteSeparatorsFinder(words)); // Very long

        // Build an alternation with all the misspelling words and find the regex in the text
        // finders.add(new WordRegexAlternateFinder(words)); // Very long
        // finders.add(new WordAutomatonAlternateFinder(words)); // Discarded: we need to increase too much the stack size
        // finders.add(new WordRegexAlternateCompleteFinder(words)); // Long
        // finders.add(new WordRegexAlternateCompleteSeparatorsFinder(words)); // Long

        // Find all words in the text and check if they are in the list
        finders.add(new WordRegexAllFinder(words));
        finders.add(new WordAutomatonAllFinder(words));
        finders.add(new WordLinearAllFinder(words));
        finders.add(new WordRegexAllCompleteFinder(words));

        List<Finder<?>> benchmarkFinders = new ArrayList<>(finders);
        runBenchmark(benchmarkFinders, fileName);

        assertTrue(true);
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        generateBoxplot(fileName, "Simple Misspelling");
    }
}
