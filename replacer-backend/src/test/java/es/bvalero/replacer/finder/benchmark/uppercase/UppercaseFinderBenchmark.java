package es.bvalero.replacer.finder.benchmark.uppercase;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.misspelling.Misspelling;
import es.bvalero.replacer.finder.misspelling.MisspellingManager;
import es.bvalero.replacer.finder.misspelling.UppercaseAfterFinder;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaServiceOfflineImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class UppercaseFinderBenchmark extends BaseFinderBenchmark {

    @Test
    void testBenchmark() throws Exception {
        WikipediaService wikipediaService = new WikipediaServiceOfflineImpl();
        String text = wikipediaService.getMisspellingListPageContent(WikipediaLanguage.SPANISH);

        // Load the misspellings
        MisspellingManager misspellingManager = new MisspellingManager();
        Set<Misspelling> misspellings = misspellingManager.parseItemsText(text);
        UppercaseAfterFinder uppercaseAfterFinder = new UppercaseAfterFinder();
        Set<String> words = uppercaseAfterFinder.getUppercaseWords(misspellings);

        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new UppercaseIndexOfFinder(words));
        // finders.add(new UppercaseRegexIterateFinder(words)); // Long
        // finders.add(new UppercaseAutomatonIterateFinder(words)); // Medium
        // finders.add(new UppercaseRegexLookBehindFinder(words)); // Very long
        finders.add(new UppercaseRegexAlternateFinder(words));
        finders.add(new UppercaseAutomatonAlternateFinder(words));
        finders.add(new UppercaseRegexAlternateLookBehindFinder(words));

        runBenchmark(finders);

        MatcherAssert.assertThat(true, is(true));
    }
}
