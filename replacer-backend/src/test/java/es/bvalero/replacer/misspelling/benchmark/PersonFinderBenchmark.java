package es.bvalero.replacer.misspelling.benchmark;

import es.bvalero.replacer.authentication.AuthenticationServiceImpl;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaServiceImpl;
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
@ContextConfiguration(classes = {WikipediaServiceImpl.class, AuthenticationServiceImpl.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class PersonFinderBenchmark {

    private final static int ITERATIONS = 1000;

    @Autowired
    private WikipediaService wikipediaService;

    @Test
    public void testBenchmark() throws IOException, WikipediaException, URISyntaxException {
        Collection<String> words = Arrays.asList("Domingo", "Frances", "Julio", "Sidney");

        // Load IDs of the sample articles
        List<Integer> sampleIds = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(PersonFinderBenchmark.class.getResource("/benchmark/sample-articles.txt").toURI()))) {
            stream.forEach(line -> sampleIds.add(Integer.valueOf(line.trim())));
        }

        // Load sample articles
        Map<Integer, String> sampleContents = wikipediaService.getPagesContent(sampleIds, null);

        // Load the finders
        List<PersonAbstractFinder> finders = new ArrayList<>();
        finders.add(new PersonIndexOfFinder(words)); // BEST
        finders.add(new PersonRegexFinder(words));
        finders.add(new PersonAutomatonFinder(words));
        finders.add(new PersonRegexCompleteFinder(words));
        finders.add(new PersonAutomatonCompleteFinder(words));
        finders.add(new PersonAlternateRegexFinder(words));
        finders.add(new PersonAlternateAutomatonFinder(words)); // GOOD
        finders.add(new PersonAlternateRegexCompleteFinder(words));
        finders.add(new PersonAlternateAutomatonCompleteFinder(words)); // GOOD
        finders.add(new PersonRegexAllFinder(words));
        finders.add(new PersonAutomatonAllFinder(words));
        finders.add(new PersonRegexAllCompleteFinder(words));

        System.out.println();
        System.out.println("FINDER\tTIME");
        sampleContents.values().forEach(value -> {
            for (PersonAbstractFinder finder : finders) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < ITERATIONS; i++) {
                    finder.findMatches(value);
                }
                long end = System.currentTimeMillis() - start;
                System.out.println(finder.getClass().getSimpleName() + "\t" + end);
            }
        });
    }

}
