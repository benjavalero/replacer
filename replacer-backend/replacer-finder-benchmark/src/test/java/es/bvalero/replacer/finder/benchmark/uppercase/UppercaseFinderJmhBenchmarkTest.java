package es.bvalero.replacer.finder.benchmark.uppercase;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.immutable.finders.UppercaseFinder;
import es.bvalero.replacer.finder.listing.StandardMisspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

public class UppercaseFinderJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "uppercase/uppercase-summary-jmh";

    private UppercaseIndexOfFinder uppercaseIndexOfFinder;
    private UppercaseAllWordsFinder uppercaseAllWordsFinder;
    // private UppercaseRegexIterateFinder uppercaseRegexIterateFinder; // Long
    private UppercaseAutomatonIterateFinder uppercaseAutomatonIterateFinder;
    // private UppercaseRegexLookBehindFinder uppercaseRegexLookBehindFinder; // Very long
    private UppercaseRegexAlternateFinder uppercaseRegexAlternateFinder;
    private UppercaseAutomatonAlternateFinder uppercaseAutomatonAlternateFinder;
    private UppercaseRegexAlternateLookBehindFinder uppercaseRegexAlternateLookBehindFinder;
    private UppercaseAutomatonAlternateAllFinder uppercaseAutomatonAlternateAllFinder;
    private UppercaseAhoCorasickWholeLongestFinder uppercaseAhoCorasickWholeLongestFinder;

    @Override
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
        Set<String> words = new HashSet<>(
            uppercaseFinder.getUppercaseWords(misspellings).get(WikipediaLanguage.getDefault())
        );

        // Initialize the finders
        uppercaseIndexOfFinder = new UppercaseIndexOfFinder(words);
        uppercaseAllWordsFinder = new UppercaseAllWordsFinder(words);
        uppercaseAutomatonIterateFinder = new UppercaseAutomatonIterateFinder(words);
        uppercaseRegexAlternateFinder = new UppercaseRegexAlternateFinder(words);
        uppercaseAutomatonAlternateFinder = new UppercaseAutomatonAlternateFinder(words);
        uppercaseRegexAlternateLookBehindFinder = new UppercaseRegexAlternateLookBehindFinder(words);
        uppercaseAutomatonAlternateAllFinder = new UppercaseAutomatonAlternateAllFinder(words);
        uppercaseAhoCorasickWholeLongestFinder = new UppercaseAhoCorasickWholeLongestFinder(words);
    }

    @Benchmark
    public void uppercaseIndexOfFinder(Blackhole bh) {
        runFinder(uppercaseIndexOfFinder, bh);
    }

    @Benchmark
    public void uppercaseAllWordsFinder(Blackhole bh) {
        runFinder(uppercaseAllWordsFinder, bh);
    }

    @Benchmark
    public void uppercaseAutomatonIterateFinder(Blackhole bh) {
        runFinder(uppercaseAutomatonIterateFinder, bh);
    }

    @Benchmark
    public void uppercaseRegexAlternateFinder(Blackhole bh) {
        runFinder(uppercaseRegexAlternateFinder, bh);
    }

    @Benchmark
    public void uppercaseAutomatonAlternateFinder(Blackhole bh) {
        runFinder(uppercaseAutomatonAlternateFinder, bh);
    }

    @Benchmark
    public void uppercaseAutomatonAlternateAllFinder(Blackhole bh) {
        runFinder(uppercaseAutomatonAlternateAllFinder, bh);
    }

    @Benchmark
    public void uppercaseRegexAlternateLookBehindFinder(Blackhole bh) {
        runFinder(uppercaseRegexAlternateLookBehindFinder, bh);
    }

    @Benchmark
    public void uppercaseAhoCorasickWholeLongestFinder(Blackhole bh) {
        runFinder(uppercaseAhoCorasickWholeLongestFinder, bh);
    }

    @Test
    void testGenerateChartBoxplot() throws ReplacerException {
        generateChart(fileName);
        assertTrue(true);
    }

    public static void main(String[] args) throws RunnerException {
        run(UppercaseFinderJmhBenchmarkTest.class, fileName);
    }
}
