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
import java.util.*;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {WikipediaService.class, WikipediaFacade.class, AuthenticationService.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class PersonFinderBenchmarkTest {

    private final static int ITERATIONS = 1000;

    @Autowired
    private WikipediaService wikipediaService;

    @Test
    @Ignore
    public void testBenchmark() throws IOException, WikipediaException, URISyntaxException {
        Collection<String> words = Arrays.asList("Domingo", "Frances", "Julio", "Sidney");

        // Load IDs of the sample articles
        List<Integer> sampleIds = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(PersonFinderBenchmarkTest.class.getResource("/benchmark/sample-articles.txt").toURI()))) {
            stream.forEach(line -> sampleIds.add(Integer.valueOf(line.trim())));
        }

        // Load sample articles
        Map<Integer, String> sampleContents = wikipediaService.getPagesContent(sampleIds, null);

        // Load the finders
        List<WordFinder> finders = new ArrayList<>();
        finders.add(new PersonIndexOfFinder(words));
        finders.add(new PersonMatchFinder(words));
        finders.add(new PersonAutomatonFinder(words));
        finders.add(new PersonMatchCompleteFinder(words));
        finders.add(new PersonAutomatonCompleteFinder(words));
        finders.add(new PersonRegexAlternateFinder(words));
        finders.add(new PersonAutomatonAlternateFinder(words));
        finders.add(new PersonRegexAlternateCompleteFinder(words));
        finders.add(new PersonAutomatonAlternateCompleteFinder(words)); // WINNER
        finders.add(new PersonAutomatonAllFinder(words));

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
