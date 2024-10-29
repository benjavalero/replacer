package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.util.Set;
import java.util.stream.Collectors;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

public class SimpleMisspellingFinderJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "word/simple-misspelling-summary-jmh";

    private WordRegexFinder wordRegexFinder;
    private WordRegexCompleteFinder wordRegexCompleteFinder;
    private WordRegexCompleteSeparatorsFinder wordRegexCompleteSeparatorsFinder;
    // private WordAutomatonFinder wordAutomatonFinder; // Discarded: we need to increase too much the heap size
    private WordLinearFinder wordLinearFinder;
    private WordRegexAlternateFinder wordRegexAlternateFinder;
    private WordRegexAlternateCompleteFinder wordRegexAlternateCompleteFinder;
    private WordRegexAlternateCompleteSeparatorsFinder wordRegexAlternateCompleteSeparatorsFinder;
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
    public void setUp() throws WikipediaException {
        // Base set-up
        super.setUp();

        // Load the uppercase misspellings
        Set<String> words;
        try {
            ListingFinder listingFinder = new ListingOfflineFinder();
            SimpleMisspellingLoader simpleMisspellingLoader = new SimpleMisspellingLoader(
                listingFinder,
                new SimpleMisspellingParser()
            );
            Set<SimpleMisspelling> misspellings = simpleMisspellingLoader.parseListing(
                listingFinder.getSimpleMisspellingListing(WikipediaLanguage.getDefault())
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
