package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.authentication.AuthenticationService;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaFacade;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {MisspellingManager.class, MisspellingFinder.class, WikipediaService.class, WikipediaFacade.class, AuthenticationService.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class WordFinderBenchmarkTest {

    private final static int ITERATIONS = 10000;

    @Autowired
    private MisspellingManager misspellingManager;

    @Autowired
    private MisspellingFinder misspellingFinder;

    @Autowired
    private WikipediaService wikipediaService;

    private Collection<String> words;

    @Test
    @Ignore
    public void testBenchmark() throws IOException, WikipediaException, URISyntaxException {
        // Load the misspellings
        this.words = new ArrayList<>();
        this.misspellingManager.findWikipediaMisspellings().forEach(misspelling -> {
            String word = misspelling.getWord();
            if (misspelling.isCaseSensitive()) {
                this.words.add(word);
            } else {
                // If case-insensitive, we add to the map "word" and "Word".
                this.words.add(word);
                this.words.add(misspellingFinder.setFirstUpperCase(word));
            }
        });

        // Load IDs of the sample articles
        List<Integer> sampleIds = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(WordFinderBenchmarkTest.class.getResource("/benchmark/sample-articles.txt").toURI()))) {
            stream.forEach(line -> sampleIds.add(Integer.valueOf(line.trim())));
        }

        // Load sample articles
        Map<Integer, String> sampleContents = wikipediaService.getPagesContent(sampleIds, null);

        // Load the finders
        List<WordFinder> finders = new ArrayList<>();
        // finders.add(new WordIndexOfFinder(this.words)); // 0.75 x WordMatchFinder
        // finders.add(new WordMatchFinder(this.words)); // MUCH slower than the rest of finders, from WordMatchAllFinder.
        // finders.add(new WordAutomatonFinder(this.words)); // 1 x WordMatchFinder. To run this, we need to increase the heap.
        // finders.add(new WordMatchCompleteFinder(this.words)); // 10 x WordMatchFinder
        // finders.add(new WordRegexAlternateFinder(this.words)); // 4 x WordMatchFinder. To run this, we need to increase the stack: -Xss3m
        // finders.add(new WordAutomatonAlternateFinder(this.words)); // 4 x WordMatchFinder
        // finders.add(new WordRegexAlternateCompleteFinder(this.words)); // 1 x WordMatchFinder
        finders.add(new WordMatchAllFinder(this.words));
        finders.add(new WordAutomatonAllFinder(this.words));
        finders.add(new WordMatchAllPossessiveFinder(this.words));
        finders.add(new WordMatchAllCompleteFinder(this.words));
        finders.add(new WordMatchAllCompletePossessiveFinder(this.words));

        System.out.println();
        System.out.println("FINDER\tTIME");
        sampleContents.values().forEach(value -> {
            for (WordFinder finder : finders) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < ITERATIONS; i++) {
                    finder.findWords(value);
                }
                long end = System.currentTimeMillis() - start;
                System.out.println(finder.getClass().getSimpleName() + "\t" + end);
            }
        });
    }

}
