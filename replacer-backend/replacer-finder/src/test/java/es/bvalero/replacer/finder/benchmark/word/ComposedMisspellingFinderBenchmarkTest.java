package es.bvalero.replacer.finder.benchmark.word;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.listing.ComposedMisspelling;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import es.bvalero.replacer.finder.replacement.finders.MisspellingComposedFinder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ComposedMisspellingFinderBenchmarkTest extends BaseFinderBenchmark {

    private static final String fileName = "word/composed-misspelling-benchmark.csv";

    @Test
    void testBenchmark() throws ReplacerException {
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
        Set<String> words = misspellingMap.keySet();

        // NOTE: We can use the same finders that we use for simple misspellings just with a different set of words,
        // except the finders finding all the words in the text as here we might search several words.

        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();

        // Loop over all the misspelling expressions and find them in the text with a regex
        finders.add(new WordRegexFinder(words));
        // finders.add(new WordRegexCompleteFinder(words)); // Long
        // finders.add(new WordRegexCompleteSeparatorsFinder(words)); // Very Long
        finders.add(new WordAutomatonFinder(words));
        finders.add(new WordLinearFinder(words));

        // Build an alternation with all the misspelling expressions and find the regex in the text
        // finders.add(new WordRegexAlternateFinder(words)); // Medium
        finders.add(new WordRegexAlternateCompleteFinder(words));
        finders.add(new WordRegexAlternateCompleteSeparatorsFinder(words));
        finders.add(new WordAutomatonAlternateFinder(words));

        List<Finder<?>> benchmarkFinders = new ArrayList<>(finders);
        runBenchmark(benchmarkFinders, WARM_UP / 10, ITERATIONS / 10, fileName);

        assertTrue(true);
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        generateBoxplot(fileName, "Composed Misspelling");
    }
}
