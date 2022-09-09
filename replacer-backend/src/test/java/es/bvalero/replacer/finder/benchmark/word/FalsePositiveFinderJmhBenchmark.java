package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.listing.FalsePositive;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class FalsePositiveFinderJmhBenchmark extends BaseFinderJmhBenchmark {

    private final Set<String> words = new HashSet<>();
    private WordLinearFinder wordLinearFinder;
    private WordRegexFinder wordRegexFinder;
    private WordAutomatonFinder wordAutomatonFinder;
    private WordAutomatonAlternateFinder wordAutomatonAlternateFinder;
    private WordRegexAlternateCompleteFinder wordRegexAlternateCompleteFinder;
    private WordRegexAlternateCompleteSeparatorsFinder wordRegexAlternateCompleteSeparatorsFinder;

    @Setup
    public void setUp() throws WikipediaException, ReplacerException {
        super.setUp();

        // Load the false positives
        FalsePositiveLoader falsePositiveLoader = new FalsePositiveLoader();
        ListingFinder listingFinder = new ListingOfflineFinder();
        falsePositiveLoader.setFalsePositiveParser(new FalsePositiveParser());
        Set<FalsePositive> falsePositives = falsePositiveLoader.parseListing(
            listingFinder.getFalsePositiveListing(WikipediaLanguage.getDefault())
        );

        // Extract the false positive expressions
        this.words.addAll(falsePositives.stream().map(FalsePositive::getExpression).collect(Collectors.toSet()));

        this.wordLinearFinder = new WordLinearFinder(words);
        this.wordRegexFinder = new WordRegexFinder(words);
        this.wordAutomatonFinder = new WordAutomatonFinder(words);
        this.wordAutomatonAlternateFinder = new WordAutomatonAlternateFinder(words);
        this.wordRegexAlternateCompleteFinder = new WordRegexAlternateCompleteFinder(words);
        this.wordRegexAlternateCompleteSeparatorsFinder = new WordRegexAlternateCompleteSeparatorsFinder(words);
    }

    @Benchmark
    public void testWordRegexFinder() {
        runFinder(wordRegexFinder);
    }

    @Benchmark
    public void testWordAutomatonFinder() {
        runFinder(wordAutomatonFinder);
    }

    @Benchmark
    public void testWordLinearFinder() {
        runFinder(wordLinearFinder);
    }

    @Benchmark
    public void testWordAutomatonAlternateFinder() {
        runFinder(wordAutomatonAlternateFinder);
    }

    @Benchmark
    public void testWordRegexAlternateCompleteFinder() {
        runFinder(wordRegexAlternateCompleteFinder);
    }

    @Benchmark
    public void testWordRegexAlternateCompleteSeparatorsFinder() {
        runFinder(wordRegexAlternateCompleteSeparatorsFinder);
    }
}
