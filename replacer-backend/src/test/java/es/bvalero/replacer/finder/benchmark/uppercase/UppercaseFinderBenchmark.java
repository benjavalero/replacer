package es.bvalero.replacer.finder.benchmark.uppercase;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.misspelling.*;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaServiceOfflineImpl;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

public class UppercaseFinderBenchmark extends BaseFinderBenchmark {

    @Test
    void testBenchmark() throws IOException, URISyntaxException, ReplacerException {
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
        finders.add(new UppercaseRegexIterateFinder(words));
        finders.add(new UppercaseAutomatonIterateFinder(words));
        finders.add(new UppercaseRegexLookBehindFinder(words));
        finders.add(new UppercaseRegexAlternateFinder(words));
        finders.add(new UppercaseAutomatonAlternateFinder(words));
        finders.add(new UppercaseRegexAlternateLookBehindFinder(words));

        runBenchmark(finders);

        MatcherAssert.assertThat(true, is(true));
    }
}
