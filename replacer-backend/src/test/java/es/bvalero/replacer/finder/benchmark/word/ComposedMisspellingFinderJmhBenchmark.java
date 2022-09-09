package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.listing.ComposedMisspelling;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import es.bvalero.replacer.finder.replacement.finders.MisspellingComposedFinder;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class ComposedMisspellingFinderJmhBenchmark extends BaseFinderJmhBenchmark {

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

        // Load the misspellings
        ComposedMisspellingLoader composedMisspellingLoader = new ComposedMisspellingLoader();
        ListingFinder listingFinder = new ListingOfflineFinder();
        composedMisspellingLoader.setComposedMisspellingParser(new ComposedMisspellingParser());
        Set<ComposedMisspelling> misspellings = composedMisspellingLoader.parseListing(
            listingFinder.getComposedMisspellingListing(WikipediaLanguage.getDefault())
        );

        // Extract the misspelling words
        MisspellingComposedFinder misspellingComposedFinder = new MisspellingComposedFinder();
        Map<String, Misspelling> misspellingMap = misspellingComposedFinder.buildMisspellingMap(
            misspellings.stream().map(cm -> (Misspelling) cm).collect(Collectors.toSet())
        );
        this.words.addAll(misspellingMap.keySet());

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
