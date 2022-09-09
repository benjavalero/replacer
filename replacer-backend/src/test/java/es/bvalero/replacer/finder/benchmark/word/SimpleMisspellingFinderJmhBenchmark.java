package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import es.bvalero.replacer.finder.replacement.finders.MisspellingSimpleFinder;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.openjdk.jmh.annotations.*;

@State(Scope.Benchmark)
public class SimpleMisspellingFinderJmhBenchmark extends BaseFinderJmhBenchmark {

    private final Set<String> words = new HashSet<>();
    private WordLinearAllFinder wordLinearAllFinder;
    private WordRegexAllFinder wordRegexAllFinder;
    private WordAutomatonAllFinder wordAutomatonAllFinder;
    private WordRegexAllCompleteFinder wordRegexAllCompleteFinder;

    @Setup
    public void setUp() throws WikipediaException, ReplacerException {
        super.setUp();

        // Load the misspellings
        SimpleMisspellingLoader simpleMisspellingLoader = new SimpleMisspellingLoader();
        ListingFinder listingFinder = new ListingOfflineFinder();
        simpleMisspellingLoader.setSimpleMisspellingParser(new SimpleMisspellingParser());
        Set<SimpleMisspelling> misspellings = simpleMisspellingLoader.parseListing(
            listingFinder.getSimpleMisspellingListing(WikipediaLanguage.getDefault())
        );

        // Extract the misspelling words
        MisspellingSimpleFinder misspellingSimpleFinder = new MisspellingSimpleFinder();
        Map<String, Misspelling> misspellingMap = misspellingSimpleFinder.buildMisspellingMap(
            misspellings.stream().map(sm -> (Misspelling) sm).collect(Collectors.toSet())
        );
        this.words.addAll(misspellingMap.keySet());

        this.wordLinearAllFinder = new WordLinearAllFinder(words);
        this.wordRegexAllFinder = new WordRegexAllFinder(words);
        this.wordAutomatonAllFinder = new WordAutomatonAllFinder(words);
        this.wordRegexAllCompleteFinder = new WordRegexAllCompleteFinder(words);
    }

    @Benchmark
    public void testWordRegexAllFinder() {
        runFinder(wordRegexAllFinder);
    }

    @Benchmark
    public void testWordAutomatonAllFinder() {
        runFinder(wordAutomatonAllFinder);
    }

    @Benchmark
    public void testWordLinearAllFinder() {
        runFinder(wordLinearAllFinder);
    }

    @Benchmark
    public void testWordRegexAllCompleteFinder() {
        runFinder(wordRegexAllCompleteFinder);
    }
}
