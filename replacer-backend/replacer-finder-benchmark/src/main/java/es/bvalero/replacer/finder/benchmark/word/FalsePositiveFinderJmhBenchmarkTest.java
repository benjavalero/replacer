package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.listing.FalsePositive;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
import java.util.Set;
import java.util.stream.Collectors;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

public class FalsePositiveFinderJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "word/false-positive-summary-jmh";

    private WordRegexFinder wordRegexFinder;
    // private WordRegexCompleteFinder wordRegexCompleteFinder; // Long
    // private WordRegexCompleteSeparatorsFinder wordRegexCompleteSeparatorsFinder; // Very long
    private WordAutomatonFinder wordAutomatonFinder;
    private WordLinearFinder wordLinearFinder;
    // private WordRegexAlternateFinder wordRegexAlternateFinder; // Medium
    private WordRegexAlternateCompleteFinder wordRegexAlternateCompleteFinder;
    private WordRegexAlternateCompleteSeparatorsFinder wordRegexAlternateCompleteSeparatorsFinder;
    private WordAutomatonAlternateFinder wordAutomatonAlternateFinder;
    private WordAhoCorasickFinder wordAhoCorasickFinder;
    private WordAhoCorasickLongestFinder wordAhoCorasickLongestFinder;
    private WordAhoCorasickWholeLongestFinder wordAhoCorasickWholeLongestFinder;

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
        Set<String> words = falsePositives.stream().map(FalsePositive::getExpression).collect(Collectors.toSet());

        // Initialize the finders
        wordRegexFinder = new WordRegexFinder(words);
        wordAutomatonFinder = new WordAutomatonFinder(words);
        wordLinearFinder = new WordLinearFinder(words);
        wordRegexAlternateCompleteFinder = new WordRegexAlternateCompleteFinder(words);
        wordRegexAlternateCompleteSeparatorsFinder = new WordRegexAlternateCompleteSeparatorsFinder(words);
        wordAutomatonAlternateFinder = new WordAutomatonAlternateFinder(words);
        wordAhoCorasickFinder = new WordAhoCorasickFinder(words);
        wordAhoCorasickLongestFinder = new WordAhoCorasickLongestFinder(words);
        wordAhoCorasickWholeLongestFinder = new WordAhoCorasickWholeLongestFinder(words);
    }

    @Benchmark
    public void wordRegexFinder(Blackhole bh) {
        runFinder(wordRegexFinder, bh);
    }

    @Benchmark
    public void wordAutomatonFinder(Blackhole bh) {
        runFinder(wordAutomatonFinder, bh);
    }

    @Benchmark
    public void wordLinearFinder(Blackhole bh) {
        runFinder(wordLinearFinder, bh);
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
    public void wordAutomatonAlternateFinder(Blackhole bh) {
        runFinder(wordAutomatonAlternateFinder, bh);
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
    public void wordAhoCorasickWholeLongestFinder(Blackhole bh) {
        runFinder(wordAhoCorasickWholeLongestFinder, bh);
    }

    public static void main(String[] args) throws RunnerException {
        run(FalsePositiveFinderJmhBenchmarkTest.class, fileName);
    }
}
