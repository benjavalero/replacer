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
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

public class ComposedMisspellingFinderJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "word/composed-misspelling-summary-jmh";

    private WordRegexFinder wordRegexFinder;
    private WordRegexCompleteFinder wordRegexCompleteFinder;
    private WordRegexCompleteSeparatorsFinder wordRegexCompleteSeparatorsFinder;
    // private WordAutomatonFinder wordAutomatonFinder; // Heap space issues
    private WordLinearFinder wordLinearFinder;
    private WordRegexAlternateFinder wordRegexAlternateFinder;
    private WordRegexAlternateCompleteFinder wordRegexAlternateCompleteFinder;
    private WordRegexAlternateCompleteSeparatorsFinder wordRegexAlternateCompleteSeparatorsFinder;
    // private WordAutomatonAlternateFinder wordAutomatonAlternateFinder; // Heap space issues
    private WordAhoCorasickFinder wordAhoCorasickFinder;
    private WordAhoCorasickLongestFinder wordAhoCorasickLongestFinder;
    private WordAhoCorasickWholeLongestFinder wordAhoCorasickWholeLongestFinder;

    @Setup
    public void setUp() throws WikipediaException {
        // Base set-up
        super.setUp();

        // Load the uppercase misspellings
        Set<String> words;
        try {
            ListingFinder listingFinder = new ListingOfflineFinder();
            ComposedMisspellingLoader composedMisspellingLoader = new ComposedMisspellingLoader(
                listingFinder,
                new ComposedMisspellingParser()
            );
            Set<ComposedMisspelling> misspellings = composedMisspellingLoader.parseListing(
                listingFinder.getComposedMisspellingListing(WikipediaLanguage.getDefault())
            );

            // Extract the misspelling words
            words = misspellings.stream().flatMap(cm -> cm.getTerms().stream()).collect(Collectors.toSet());
        } catch (ReplacerException e) {
            throw new WikipediaException(e);
        }

        // Initialize the finders
        wordRegexFinder = new WordRegexFinder(words);
        wordRegexCompleteFinder = new WordRegexCompleteFinder(words);
        wordRegexCompleteSeparatorsFinder = new WordRegexCompleteSeparatorsFinder(words);
        // wordAutomatonFinder = new WordAutomatonFinder(words);
        wordLinearFinder = new WordLinearFinder(words);
        wordRegexAlternateFinder = new WordRegexAlternateFinder(words);
        wordRegexAlternateCompleteFinder = new WordRegexAlternateCompleteFinder(words);
        wordRegexAlternateCompleteSeparatorsFinder = new WordRegexAlternateCompleteSeparatorsFinder(words);
        // wordAutomatonAlternateFinder = new WordAutomatonAlternateFinder(words);
        wordAhoCorasickFinder = new WordAhoCorasickFinder(words);
        wordAhoCorasickLongestFinder = new WordAhoCorasickLongestFinder(words);
        wordAhoCorasickWholeLongestFinder = new WordAhoCorasickWholeLongestFinder(words);
    }

    @Benchmark
    public void wordRegexFinder(Blackhole bh) {
        runFinder(wordRegexFinder, bh);
    }

    @Benchmark
    public void wordRegexCompleteFinder(Blackhole bh) {
        runFinder(wordRegexCompleteFinder, bh);
    }

    @Benchmark
    public void wordRegexCompleteSeparatorsFinder(Blackhole bh) {
        runFinder(wordRegexCompleteSeparatorsFinder, bh);
    }

    /*
    @Benchmark
    public void wordAutomatonFinder(Blackhole bh) {
        runFinder(wordAutomatonFinder, bh);
    }
     */

    @Benchmark
    public void wordLinearFinder(Blackhole bh) {
        runFinder(wordLinearFinder, bh);
    }

    @Benchmark
    public void wordRegexAlternateFinder(Blackhole bh) {
        runFinder(wordRegexAlternateFinder, bh);
    }

    @Benchmark
    public void wordRegexAlternateCompleteFinder(Blackhole bh) {
        runFinder(wordRegexAlternateCompleteFinder, bh);
    }

    @Benchmark
    public void wordRegexAlternateCompleteSeparatorsFinder(Blackhole bh) {
        runFinder(wordRegexAlternateCompleteSeparatorsFinder, bh);
    }

    /*
    @Benchmark
    public void wordAutomatonAlternateFinder(Blackhole bh) {
        runFinder(wordAutomatonAlternateFinder, bh);
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
    public void wordAhoCorasickWholeLongestFinder(Blackhole bh) {
        runFinder(wordAhoCorasickWholeLongestFinder, bh);
    }

    @Test
    void testBenchmark() throws RunnerException {
        run(ComposedMisspellingFinderJmhBenchmarkTest.class, fileName);

        assertTrue(true);
    }
}
