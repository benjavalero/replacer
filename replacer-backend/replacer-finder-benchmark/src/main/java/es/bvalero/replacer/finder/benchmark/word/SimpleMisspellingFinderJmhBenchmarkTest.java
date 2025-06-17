package es.bvalero.replacer.finder.benchmark.word;

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
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

public class SimpleMisspellingFinderJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "word/simple-misspelling-summary-jmh";

    // private WordRegexFinder wordRegexFinder; // Medium
    // private WordRegexCompleteFinder wordRegexCompleteFinder; // Very long
    // private WordRegexCompleteSeparatorsFinder wordRegexCompleteSeparatorsFinder; // Very long
    // private WordAutomatonFinder wordAutomatonFinder; // Discarded: we need to increase too much the heap size
    // private WordLinearFinder wordLinearFinder; // Short
    // private WordRegexAlternateFinder wordRegexAlternateFinder; // Very long
    // private WordRegexAlternateCompleteFinder wordRegexAlternateCompleteFinder; // Long
    // private WordRegexAlternateCompleteSeparatorsFinder wordRegexAlternateCompleteSeparatorsFinder; // Long
    // private WordAutomatonAlternateFinder wordAutomatonAlternateFinder; // Discarded: we need to increase too much the stack size
    private WordRegexAllFinder wordRegexAllFinder;
    private WordRegexAllCompleteFinder wordRegexAllCompleteFinder;
    private WordRegexAllCompleteSeparatorsFinder wordRegexAllCompleteSeparatorsFinder;
    private WordAutomatonAllFinder wordAutomatonAllFinder;
    private WordLinearAllFinder wordLinearAllFinder;
    private WordAhoCorasickFinder wordAhoCorasickFinder;
    private WordAhoCorasickLongestFinder wordAhoCorasickLongestFinder;
    private WordAhoCorasickWholeFinder wordAhoCorasickWholeFinder;
    private WordAhoCorasickWholeLongestFinder wordAhoCorasickWholeLongestFinder;

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
        wordRegexAllFinder = new WordRegexAllFinder(words);
        wordRegexAllCompleteFinder = new WordRegexAllCompleteFinder(words);
        wordRegexAllCompleteSeparatorsFinder = new WordRegexAllCompleteSeparatorsFinder(words);
        wordAutomatonAllFinder = new WordAutomatonAllFinder(words);
        wordLinearAllFinder = new WordLinearAllFinder(words);
        wordAhoCorasickFinder = new WordAhoCorasickFinder(words);
        wordAhoCorasickLongestFinder = new WordAhoCorasickLongestFinder(words);
        wordAhoCorasickWholeFinder = new WordAhoCorasickWholeFinder(words);
        wordAhoCorasickWholeLongestFinder = new WordAhoCorasickWholeLongestFinder(words);
    }

    @Benchmark
    public void wordRegexAllFinder(Blackhole bh) {
        runFinder(wordRegexAllFinder, bh);
    }

    @Benchmark
    public void wordRegexAllCompleteFinder(Blackhole bh) {
        runFinder(wordRegexAllCompleteFinder, bh);
    }

    @Benchmark
    public void wordRegexAllCompleteSeparatorsFinder(Blackhole bh) {
        runFinder(wordRegexAllCompleteSeparatorsFinder, bh);
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
    public void wordAhoCorasickFinder(Blackhole bh) {
        runFinder(wordAhoCorasickFinder, bh);
    }

    @Benchmark
    public void wordAhoCorasickLongestFinder(Blackhole bh) {
        runFinder(wordAhoCorasickLongestFinder, bh);
    }

    @Benchmark
    public void wordAhoCorasickWholeFinder(Blackhole bh) {
        runFinder(wordAhoCorasickWholeFinder, bh);
    }

    @Benchmark
    public void wordAhoCorasickWholeLongestFinder(Blackhole bh) {
        runFinder(wordAhoCorasickWholeLongestFinder, bh);
    }

    public static void main(String[] args) throws RunnerException {
        run(SimpleMisspellingFinderJmhBenchmarkTest.class, fileName);
    }
}
