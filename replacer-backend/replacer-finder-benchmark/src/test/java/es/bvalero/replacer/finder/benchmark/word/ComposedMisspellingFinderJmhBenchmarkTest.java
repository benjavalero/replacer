package es.bvalero.replacer.finder.benchmark.word;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.listing.ComposedMisspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

public class ComposedMisspellingFinderJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "word/composed-misspelling-summary-jmh";

    // private WordRegexFinder wordRegexFinder; // Short
    // private WordRegexAlternateFinder wordRegexAlternateFinder; // Long
    // private WordAutomatonFinder wordAutomatonFinder; // Discarded: we need to increase the heap size too much
    // private WordAutomatonAlternateFinder wordAutomatonAlternateFinder; // Discarded: we need to increase the stack size too much
    // private WordLinearFinder wordLinearFinder; // Good
    private WordAhoCorasickFinder wordAhoCorasickFinder;
    private WordAhoCorasickLongestFinder wordAhoCorasickLongestFinder;
    private WordTrieFinder wordTrieFinder;
    private WordTrieNoOverlappingFinder wordTrieNoOverlappingFinder;

    @Override
    @Setup
    public void setUp() throws ReplacerException {
        // Base set-up
        super.setUp();

        // Load the uppercase misspellings
        ListingFinder listingFinder = new ListingOfflineFinder();
        ComposedMisspellingLoader composedMisspellingLoader = new ComposedMisspellingLoader(
            listingFinder,
            new ComposedMisspellingParser()
        );
        Set<ComposedMisspelling> misspellings = composedMisspellingLoader.parseListing(
            listingFinder.getComposedMisspellingListing(WikipediaLanguage.getDefault())
        );

        // Extract the misspelling words
        Set<String> words = misspellings.stream().flatMap(cm -> cm.getTerms().stream()).collect(Collectors.toSet());

        // Initialize the finders
        // wordRegexFinder = new WordRegexFinder(words);
        // wordRegexAlternateFinder = new WordRegexAlternateFinder(words);
        // wordLinearFinder = new WordLinearFinder(words);
        wordAhoCorasickFinder = new WordAhoCorasickFinder(words);
        wordAhoCorasickLongestFinder = new WordAhoCorasickLongestFinder(words);
        wordTrieFinder = new WordTrieFinder(words);
        wordTrieNoOverlappingFinder = new WordTrieNoOverlappingFinder(words);
    }

    /*
    @Benchmark
    public void wordRegexFinder(Blackhole bh) {
        runFinder(wordRegexFinder, bh);
    }

    @Benchmark
    public void wordRegexAlternateFinder(Blackhole bh) {
        runFinder(wordRegexAlternateFinder, bh);
    }

    @Benchmark
    public void wordLinearFinder(Blackhole bh) {
        runFinder(wordLinearFinder, bh);
    }
    */

    @Benchmark
    public void wordAhoCorasickFinder(Blackhole bh) {
        runFinder(wordAhoCorasickFinder, bh);
    }

    @Benchmark
    public void wordAhoCorasickLongestFinder(Blackhole bh) {
        runFinder(wordAhoCorasickLongestFinder, bh);
    }

    @Benchmark
    public void wordTrieFinder(Blackhole bh) {
        runFinder(wordTrieFinder, bh);
    }

    @Benchmark
    public void wordTrieNoOverlappingFinder(Blackhole bh) {
        runFinder(wordTrieNoOverlappingFinder, bh);
    }

    @Test
    void testGenerateChartBoxplot() throws ReplacerException {
        generateChart(fileName);
        assertTrue(true);
    }

    public static void main(String[] args) throws RunnerException {
        run(ComposedMisspellingFinderJmhBenchmarkTest.class, fileName);
    }
}
