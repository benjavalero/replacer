package es.bvalero.replacer.finder.benchmark.word;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.listing.FalsePositive;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

public class FalsePositiveFinderJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "word/false-positive-summary-jmh";

    // private WordRegexFinder wordRegexFinder; // Short
    // private WordRegexCompleteFinder wordRegexCompleteFinder; // Very long
    // private WordRegexCompleteSeparatorsFinder wordRegexCompleteSeparatorsFinder; // Very long
    // private WordRegexAlternateFinder wordRegexAlternateFinder; // Long
    // private WordRegexAlternateCompleteFinder wordRegexAlternateCompleteFinder; // Long
    // private WordRegexAlternateCompleteSeparatorsFinder wordRegexAlternateCompleteSeparatorsFinder; // Medium
    // private WordAutomatonFinder wordAutomatonFinder; // Long
    // private WordAutomatonAlternateFinder wordAutomatonAlternateFinder; // Good - No overlapping
    // private WordAutomatonAlternateFinder wordAutomatonAlternateRegexFinder; // Good - No overlapping
    // private WordLinearFinder wordLinearFinder; // Long
    private WordAhoCorasickFinder wordAhoCorasickFinder; // Good - Allows overlapping
    // private WordAhoCorasickLongestFinder wordAhoCorasickLongestFinder; // Good - No overlapping
    // private WordAhoCorasickWholeLongestFinder wordAhoCorasickWholeLongestFinder; // Best - No complete overlapping
    private WordTrieFinder wordTrieFinder; // Good - Allows overlapping
    // private WordTrieNoOverlappingFinder wordTrieNoOverlappingFinder; // Good - No complete overlapping
    private WordTrieWholeFinder wordTrieWholeFinder; // Good - Allows overlapping

    @Setup
    public void setUp() throws ReplacerException {
        // Base set-up
        super.setUp();

        // Load the uppercase misspellings
        ListingFinder listingFinder = new ListingOfflineFinder();
        FalsePositiveLoader falsePositiveLoader = new FalsePositiveLoader(listingFinder, new FalsePositiveParser());
        Set<FalsePositive> falsePositives = falsePositiveLoader.parseListing(
            listingFinder.getFalsePositiveListing(WikipediaLanguage.getDefault())
        );

        // Extract the misspelling words
        Set<String> regex = falsePositives.stream().map(FalsePositive::getExpression).collect(Collectors.toSet());
        Set<String> words = regex
            .stream()
            .flatMap(w -> FinderUtils.expandRegex(w).stream())
            .collect(Collectors.toSet());

        // Initialize the finders
        // wordRegexFinder = new WordRegexFinder(words);
        // wordRegexCompleteFinder = new WordRegexCompleteFinder(words);
        // wordRegexCompleteSeparatorsFinder = new WordRegexCompleteSeparatorsFinder(words);
        // wordRegexAlternateFinder = new WordRegexAlternateFinder(words);
        // wordRegexAlternateCompleteFinder = new WordRegexAlternateCompleteFinder(words);
        // wordRegexAlternateCompleteSeparatorsFinder = new WordRegexAlternateCompleteSeparatorsFinder(words);
        // wordAutomatonFinder = new WordAutomatonFinder(words);
        // wordAutomatonAlternateFinder = new WordAutomatonAlternateFinder(words);
        // wordAutomatonAlternateRegexFinder = new WordAutomatonAlternateFinder(regex, false);
        // wordLinearFinder = new WordLinearFinder(words);
        wordAhoCorasickFinder = new WordAhoCorasickFinder(words);
        // wordAhoCorasickLongestFinder = new WordAhoCorasickLongestFinder(words);
        // wordAhoCorasickWholeLongestFinder = new WordAhoCorasickWholeLongestFinder(words);
        wordTrieFinder = new WordTrieFinder(words);
        // wordTrieNoOverlappingFinder = new WordTrieNoOverlappingFinder(words);
        wordTrieWholeFinder = new WordTrieWholeFinder(words);
    }

    /*
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

    @Benchmark
    public void wordAutomatonFinder(Blackhole bh) {
        runFinder(wordAutomatonFinder, bh);
    }

    @Benchmark
    public void wordAutomatonAlternateFinder(Blackhole bh) {
        runFinder(wordAutomatonAlternateFinder, bh);
    }

    @Benchmark
    public void wordAutomatonAlternateRegexFinder(Blackhole bh) {
        runFinder(wordAutomatonAlternateRegexFinder, bh);
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

    /*
    @Benchmark
    public void wordAhoCorasickLongestFinder(Blackhole bh) {
        runFinder(wordAhoCorasickLongestFinder, bh);
    }

    @Benchmark
    public void wordAhoCorasickWholeLongestFinder(Blackhole bh) {
        runFinder(wordAhoCorasickWholeLongestFinder, bh);
    }
    */

    @Benchmark
    public void wordTrieFinder(Blackhole bh) {
        runFinder(wordTrieFinder, bh);
    }

    /*
    @Benchmark
    public void wordTrieNoOverlappingFinder(Blackhole bh) {
        runFinder(wordTrieNoOverlappingFinder, bh);
    }
    */

    @Benchmark
    public void wordTrieWholeFinder(Blackhole bh) {
        runFinder(wordTrieWholeFinder, bh);
    }

    @Test
    void testGenerateChartBoxplot() throws ReplacerException {
        generateChart(fileName);
        assertTrue(true);
    }

    public static void main(String[] args) throws RunnerException {
        run(FalsePositiveFinderJmhBenchmarkTest.class, fileName);
    }
}
