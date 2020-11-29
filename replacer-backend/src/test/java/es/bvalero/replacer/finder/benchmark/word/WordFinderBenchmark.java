package es.bvalero.replacer.finder.benchmark.word;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.misspelling.Misspelling;
import es.bvalero.replacer.finder.misspelling.MisspellingFinder;
import es.bvalero.replacer.finder.misspelling.MisspellingManager;
import es.bvalero.replacer.finder.misspelling.MisspellingSimpleFinder;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaServiceOfflineImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class WordFinderBenchmark extends BaseFinderBenchmark {

    @Test
    void testWordFinderBenchmark() throws Exception {
        WikipediaService wikipediaService = new WikipediaServiceOfflineImpl();
        String text = wikipediaService.getMisspellingListPageContent(WikipediaLanguage.SPANISH);

        // Load the misspellings
        MisspellingManager misspellingManager = new MisspellingManager();
        Set<Misspelling> misspellings = misspellingManager.parseItemsText(text);
        MisspellingFinder misspellingFinder = new MisspellingSimpleFinder();
        Set<String> words = misspellingFinder.buildMisspellingMap(misspellings).keySet();

        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();

        // Loop over all the words and find them in the text with a regex
        // finders.add(new WordIndexOfFinder(words)); // Discarded: about 15 times slower
        // finders.add(new WordRegexFinder(words)); // Discarded: about 200 times slower
        // finders.add(new WordAutomatonFinder(words)); // Discarded: we need to increase too much the heap size
        // finders.add(new WordRegexCompleteFinder(words)); // Discarded: about 3000 times slower

        // Build an alternation with all the words and find the regex in the text
        // finders.add(new WordRegexAlternateFinder(words)); // Discarded: about 1000 times slower
        // finders.add(new WordAutomatonAlternateFinder(words)); // Discarded: we need to increase too much the stack size
        // finders.add(new WordRegexAlternateCompleteFinder(words)); // Discarded: about 400 times slower

        // Find all words in the text and check if they are in the list
        finders.add(new WordRegexAllFinder(words));
        finders.add(new WordAutomatonAllFinder(words));
        finders.add(new WordLinearAllFinder(words)); // Winner
        finders.add(new WordRegexAllCompleteFinder(words));

        runBenchmark(finders);

        MatcherAssert.assertThat(true, is(true));
    }
}
