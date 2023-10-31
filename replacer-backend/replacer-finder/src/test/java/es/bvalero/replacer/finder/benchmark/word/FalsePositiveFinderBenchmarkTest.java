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
    void testBenchmark() throws ReplacerException {
        // Load the false positives
        ListingFinder listingFinder = new ListingOfflineFinder();
        FalsePositiveLoader falsePositiveLoader = new FalsePositiveLoader(listingFinder, new FalsePositiveParser());
        Set<FalsePositive> falsePositives = falsePositiveLoader.parseListing(
            listingFinder.getFalsePositiveListing(WikipediaLanguage.getDefault())
        );

        // Extract the false positive expressions
        Set<String> words = falsePositives.stream().map(FalsePositive::getExpression).collect(Collectors.toSet());

        // NOTE: We can use the same finders that we use for simple misspellings just with a different set of words,
        // except the finders finding all the words in the text as here we might search several words.

        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();

        // Loop over all the false positive expressions and find them in the text with a regex
        // finders.add(new WordRegexFinder(words)); // Short
        // finders.add(new WordRegexCompleteFinder(words)); // Long
        // finders.add(new WordRegexCompleteSeparatorsFinder(words)); // Very long
        // finders.add(new WordAutomatonFinder(words)); // Short
        finders.add(new WordLinearFinder(words));

        // Build an alternation with all the false positive expressions and find the regex in the text
        // finders.add(new WordRegexAlternateFinder(words)); // Medium
        // finders.add(new WordRegexAlternateCompleteFinder(words)); // Short
        // finders.add(new WordRegexAlternateCompleteSeparatorsFinder(words)); // Short
        finders.add(new WordAutomatonAlternateFinder(words));

        // Use the Aho-Corasick algorithm which eventually creates an automaton
        // The whole-word finder cannot be used here as it doesn't work for expressions
        finders.add(new WordAhoCorasickFinder(words)); // Similar to the best automaton approach
        finders.add(new WordAhoCorasickLongestFinder(words)); // 50% worse than the simple one
        finders.add(new WordAhoCorasickWholeLongestFinder(words)); // Almost 3x faster than the simple one
        // NOTE: These finders support a case-insensitive flag but the performance is reduced significantly

        List<Finder<?>> benchmarkFinders = new ArrayList<>(finders);
        runBenchmark(benchmarkFinders, fileName);

        assertTrue(true);
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        generateBoxplot(fileName, "False Positive");
    }
}
