package es.bvalero.replacer.misspelling.benchmark;

import es.bvalero.replacer.authentication.AuthenticationServiceImpl;
import es.bvalero.replacer.misspelling.FalsePositiveFinder;
import es.bvalero.replacer.misspelling.FalsePositiveManager;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaServiceImpl;
import org.junit.Assert;
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
@ContextConfiguration(classes = {FalsePositiveFinder.class, FalsePositiveManager.class, WikipediaServiceImpl.class, AuthenticationServiceImpl.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class FalseFinderBenchmark {

    private final static int ITERATIONS = 1000;

    @Autowired
    private FalsePositiveManager falsePositiveManager;

    @Autowired
    private FalsePositiveFinder falsePositiveFinder;

    @Autowired
    private WikipediaService wikipediaService;

    /* NOTE: We can use the same finders that we use for misspellings just with a different set of words */

    @Test
    public void testWordFinderBenchmark() throws IOException, WikipediaException, URISyntaxException {
        // Load the misspellings
        falsePositiveManager.updateFalsePositives();
        Collection<String> words = falsePositiveFinder.getFalsePositives();

        // Load IDs of the sample articles
        List<Integer> sampleIds = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(FalseFinderBenchmark.class.getResource("/benchmark/sample-articles.txt").toURI()))) {
            stream.forEach(line -> sampleIds.add(Integer.valueOf(line.trim())));
        }

        // Load sample articles
        List<WikipediaPage> sampleContents = wikipediaService.getPagesByIds(sampleIds);

        // Load the finders
        List<WordAbstractFinder> finders = new ArrayList<>();
        finders.add(new WordIndexOfFinder(words));
        finders.add(new WordRegexFinder(words));
        finders.add(new WordAutomatonFinder(words));
        finders.add(new WordRegexCompleteFinder(words));
        finders.add(new WordAlternateRegexFinder(words));
        finders.add(new WordAlternateAutomatonFinder(words)); // WINNER
        finders.add(new WordAlternateRegexCompleteFinder(words));

        System.out.println();
        System.out.println("FINDER\tTIME");
        sampleContents.forEach(value -> {
            for (WordAbstractFinder finder : finders) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < ITERATIONS; i++) {
                    finder.findMatches(value.getContent());
                }
                long end = System.currentTimeMillis() - start;
                System.out.println(finder.getClass().getSimpleName() + "\t" + end);
            }
        });

        Assert.assertTrue(true);
    }

}
