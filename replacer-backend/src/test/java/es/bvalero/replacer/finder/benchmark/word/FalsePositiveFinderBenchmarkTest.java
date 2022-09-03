package es.bvalero.replacer.finder.benchmark.word;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.listing.FalsePositive;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class FalsePositiveFinderBenchmarkTest extends BaseFinderBenchmark {

    private static final String fileName = "word/false-positive-benchmark.csv";

    @Test
    void testWordFinderBenchmark() throws ReplacerException {
        // Load the false positives
        FalsePositiveLoader falsePositiveLoader = new FalsePositiveLoader();
        ListingFinder listingFinder = new ListingOfflineFinder();
        falsePositiveLoader.setFalsePositiveParser(new FalsePositiveParser());
        Set<FalsePositive> falsePositives = falsePositiveLoader.parseListing(
            listingFinder.getFalsePositiveListing(WikipediaLanguage.getDefault())
        );

        // Extract the false positive expressions
        Set<String> words = falsePositives.stream().map(FalsePositive::getExpression).collect(Collectors.toSet());

        /* NOTE: We can use the same finders that we use for misspellings just with a different set of words */

        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();

        // Loop over all the false positive expressions and find them in the text with a regex
        finders.add(new WordLinearFinder(words));
        finders.add(new WordRegexFinder(words));
        finders.add(new WordAutomatonFinder(words));
        // finders.add(new WordRegexCompleteFinder(words)); // Long
        // finders.add(new WordRegexCompleteSeparatorsFinder(words)); // Very long

        // Build an alternation with all the false positive expressions and find the regex in the text
        // finders.add(new WordRegexAlternateFinder(words)); // Medium
        finders.add(new WordAutomatonAlternateFinder(words));
        finders.add(new WordRegexAlternateCompleteFinder(words));
        finders.add(new WordRegexAlternateCompleteSeparatorsFinder(words));

        // Find all words in the text and check if they are in the list
        // finders.add(new WordRegexAllFinder(words)); // Don't work with composed
        // finders.add(new WordAutomatonAllFinder(words)); // Don't work with composed
        // finders.add(new WordLinearAllFinder(words)); // Don't work with composed
        // finders.add(new WordRegexAllCompleteFinder(words)); // Don't work with composed
        // finders.add(new WordRegexAllCompleteSeparatorsFinder(words)); // Don't work with composed

        List<Finder<?>> benchmarkFinders = new ArrayList<>(finders);
        runBenchmark(benchmarkFinders, WARM_UP / 10, ITERATIONS / 10, fileName);

        assertTrue(true);
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        generateBoxplot(fileName, "False Positive");
    }
}
