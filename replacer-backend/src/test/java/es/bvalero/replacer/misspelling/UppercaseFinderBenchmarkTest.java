package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.authentication.AuthenticationServiceImpl;
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
@ContextConfiguration(classes = {MisspellingManager.class, MisspellingFinder.class, WikipediaService.class, WikipediaFacade.class, AuthenticationServiceImpl.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class UppercaseFinderBenchmarkTest {

    private final static int ITERATIONS = 1000;

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private MisspellingManager misspellingManager;

    @Autowired
    private MisspellingFinder misspellingFinder;

    private Collection<String> words;

    @Test
    @Ignore
    public void testBenchmark() throws WikipediaException, URISyntaxException, IOException {
        // Load the misspellings
        this.words = new ArrayList<>();
        this.misspellingManager.findWikipediaMisspellings().forEach(misspelling -> {
            String word = misspelling.getWord();
            if (misspelling.isCaseSensitive()
                    && misspellingFinder.startsWithUpperCase(word)
                    && misspelling.getSuggestions().size() == 1
                    && misspelling.getSuggestions().get(0).equals(word.toLowerCase())) {
                this.words.add(word);
            }
        });

        // Load IDs of the sample articles
        List<Integer> sampleIds = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(PersonFinderBenchmarkTest.class.getResource("/benchmark/sample-articles.txt").toURI()))) {
            stream.forEach(line -> sampleIds.add(Integer.valueOf(line.trim())));
        }

        // Load sample articles
        Map<Integer, String> sampleContents = wikipediaService.getPagesContent(sampleIds, null);

        // Load the finders
        List<WordFinder> finders = new ArrayList<>();
        finders.add(new UppercaseIndexOfFinder(words));
        finders.add(new UppercaseMatchFinder(words));
        finders.add(new UppercaseAutomatonFinder(words));
        finders.add(new UppercaseMatchLookBehindFinder(words));
        finders.add(new UppercaseRegexAlternateFinder(words));
        finders.add(new UppercaseAutomatonAlternateFinder(words)); // WINNER
        finders.add(new UppercaseRegexAlternateLookBehindFinder(words));

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
