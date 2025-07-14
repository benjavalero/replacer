package es.bvalero.replacer.finder.benchmark.word;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

public class SimpleMisspellingFinderJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "word/simple-misspelling-summary-jmh";

    // private WordRegexFinder wordRegexFinder; // Medium
    // private WordRegexCompleteFinder wordRegexCompleteFinder; // Very long
    // private WordRegexCompleteSeparatorsFinder wordRegexCompleteSeparatorsFinder; // Very long
    // private WordRegexAlternateFinder wordRegexAlternateFinder; // Very long
    // private WordRegexAlternateCompleteFinder wordRegexAlternateCompleteFinder; // Long
    // private WordRegexAlternateCompleteSeparatorsFinder wordRegexAlternateCompleteSeparatorsFinder; // Long
    // private WordRegexAllFinder wordRegexAllFinder; // Good
    // private WordRegexAllCompleteFinder wordRegexAllCompleteFinder; // Good
    // private WordRegexAllCompleteSeparatorsFinder wordRegexAllCompleteSeparatorsFinder; // Good
    // private WordAutomatonFinder wordAutomatonFinder; // Discarded: we need to increase the heap size too much
    // private WordAutomatonAlternateFinder wordAutomatonAlternateFinder; // Discarded: we need to increase the stack size too much
    private WordAutomatonAllFinder wordAutomatonAllFinder;
    // private WordLinearFinder wordLinearFinder; // Short
    private WordLinearAllFinder wordLinearAllFinder;
    // private WordAhoCorasickFinder wordAhoCorasickFinder; // Good
    // private WordAhoCorasickLongestFinder wordAhoCorasickLongestFinder; // Good
    private WordAhoCorasickWholeFinder wordAhoCorasickWholeFinder;
    private WordAhoCorasickWholeLongestFinder wordAhoCorasickWholeLongestFinder;
    // private WordTrieFinder wordTrieFinder; // Good
    // private WordTrieNoOverlappingFinder wordTrieNoOverlappingFinder; // Good
    // private WordTrieWholeFinder wordTrieWholeFinder; // Good

    @Setup
    public void setUp() throws ReplacerException {
        // Base set-up
        super.setUp();

        // Load the uppercase misspellings
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

        // Initialize the finders
        wordAutomatonAllFinder = new WordAutomatonAllFinder(words);
        wordLinearAllFinder = new WordLinearAllFinder(words);
        wordAhoCorasickWholeFinder = new WordAhoCorasickWholeFinder(words);
        wordAhoCorasickWholeLongestFinder = new WordAhoCorasickWholeLongestFinder(words);
    }

    @Benchmark
    public void wordAutomatonAllFinder(Blackhole bh) {
        runFinder(wordAutomatonAllFinder, bh);
    }

    @Benchmark
    public void wordLinearAllFinder(Blackhole bh) {
        runFinder(wordLinearAllFinder, bh);
    }

    @Benchmark
    public void wordAhoCorasickWholeFinder(Blackhole bh) {
        runFinder(wordAhoCorasickWholeFinder, bh);
    }

    @Benchmark
    public void wordAhoCorasickWholeLongestFinder(Blackhole bh) {
        runFinder(wordAhoCorasickWholeLongestFinder, bh);
    }

    @Test
    void testGenerateChartBoxplot() throws ReplacerException {
        generateChart(fileName);
        assertTrue(true);
    }

    public static void main(String[] args) throws RunnerException {
        run(SimpleMisspellingFinderJmhBenchmarkTest.class, fileName);
    }
}
