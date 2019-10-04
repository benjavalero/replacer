package es.bvalero.replacer.misspelling.benchmark;

import es.bvalero.replacer.authentication.AuthenticationConfig;
import es.bvalero.replacer.misspelling.MisspellingManager;
import es.bvalero.replacer.misspelling.UppercaseAfterFinder;
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
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {UppercaseAfterFinder.class, MisspellingManager.class, WikipediaServiceImpl.class, AuthenticationConfig.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class UppercaseFinderBenchmark {

    private static final int ITERATIONS = 1000;

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private MisspellingManager misspellingManager;

    @Autowired
    private UppercaseAfterFinder uppercaseAfterFinder;

    @Test
    public void testBenchmark() throws WikipediaException, URISyntaxException, IOException {
        // Load the misspellings
        misspellingManager.update();
        Collection<String> words = uppercaseAfterFinder.getUppercaseWords();

        // Load IDs of the sample articles
        List<Integer> sampleIds = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(UppercaseFinderBenchmark.class.getResource("/benchmark/sample-articles.txt").toURI()))) {
            stream.forEach(line -> sampleIds.add(Integer.valueOf(line.trim())));
        }

        // Load sample articles
        List<WikipediaPage> sampleContents = wikipediaService.getPagesByIds(sampleIds);

        // Load the finders
        List<UppercaseAbstractFinder> finders = new ArrayList<>();
        finders.add(new UppercaseIndexOfFinder(words));
        finders.add(new UppercaseRegexFinder(words));
        finders.add(new UppercaseAutomatonFinder(words));
        finders.add(new UppercaseRegexLookBehindFinder(words));
        finders.add(new UppercaseAlternateRegexFinder(words));
        finders.add(new UppercaseAlternateAutomatonFinder(words)); // WINNER
        finders.add(new UppercaseAlternateRegexLookBehindFinder(words));

        System.out.println();
        System.out.println("FINDER\tTIME");
        sampleContents.forEach(value -> {
            for (UppercaseAbstractFinder finder : finders) {
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
