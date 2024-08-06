package es.bvalero.replacer.finder.benchmark.word;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class SimpleMisspellingFinderBenchmarkTest extends BaseFinderBenchmark {

    private static final String fileName = "word/simple-misspelling-benchmark.csv";

    @Test
    void testBenchmark() throws ReplacerException {
        // Load the misspellings
        ListingFinder listingFinder = new ListingOfflineFinder();
        SimpleMisspellingLoader simpleMisspellingLoader = new SimpleMisspellingLoader(
            listingFinder,
            new SimpleMisspellingParser()
        );
        Set<SimpleMisspelling> misspellings = simpleMisspellingLoader.parseListing(
            listingFinder.getSimpleMisspellingListing(WikipediaLanguage.getDefault())
        );

        // Extract the misspelling words
        Set<String> words = misspellings.stream().flatMap(cm -> cm.getTerms().stream()).collect(Collectors.toSet());

        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();

        // Loop over all the misspelling words and find them in the text with a regex
        // finders.add(new WordRegexFinder(words)); // Medium
        // finders.add(new WordRegexCompleteFinder(words)); // Very long
        // finders.add(new WordRegexCompleteSeparatorsFinder(words)); // Very long
        // finders.add(new WordAutomatonFinder(words)); // Discarded: we need to increase too much the heap size
        // finders.add(new WordLinearFinder(words)); // Short

        // Build an alternation with all the misspelling words and find the regex in the text
        // finders.add(new WordRegexAlternateFinder(words)); // Very long
        // finders.add(new WordRegexAlternateCompleteFinder(words)); // Long
        // finders.add(new WordRegexAlternateCompleteSeparatorsFinder(words)); // Long
        // finders.add(new WordAutomatonAlternateFinder(words)); // Discarded: we need to increase too much the stack size

        // Find all words in the text and check if they are in the list
        finders.add(new WordRegexAllFinder(words));
        finders.add(new WordRegexAllCompleteFinder(words));
        finders.add(new WordRegexAllCompleteSeparatorsFinder(words));
        finders.add(new WordAutomatonAllFinder(words));
        finders.add(new WordLinearAllFinder(words));

        // Use the Aho-Corasick algorithm which eventually creates an automaton
        finders.add(new WordAhoCorasickFinder(words));
        finders.add(new WordAhoCorasickLongestFinder(words));
        finders.add(new WordAhoCorasickWholeFinder(words));
        finders.add(new WordAhoCorasickWholeLongestFinder(words));
        // NOTE: These finders support a case-insensitive flag but the performance is reduced significantly

        List<Finder<?>> benchmarkFinders = new ArrayList<>(finders);
        runBenchmark(benchmarkFinders, fileName);

        assertTrue(true);
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        generateBoxplot(fileName, "Simple Misspelling");
    }
}
