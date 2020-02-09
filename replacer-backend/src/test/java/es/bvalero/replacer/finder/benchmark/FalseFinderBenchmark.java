package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.misspelling.FalsePositiveFinder;
import es.bvalero.replacer.finder.misspelling.FalsePositiveManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {FalsePositiveFinder.class, FalsePositiveManager.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class FalseFinderBenchmark extends BaseFinderBenchmark {

    private static final int ITERATIONS = 1000;

    @Autowired
    private FalsePositiveManager falsePositiveManager;

    @Autowired
    private FalsePositiveFinder falsePositiveFinder;

    /* NOTE: We can use the same finders that we use for misspellings just with a different set of words */

    @Test
    public void testWordFinderBenchmark() throws IOException, URISyntaxException {
        // Load the misspellings
        falsePositiveManager.update();
        Collection<String> words = falsePositiveFinder.getFalsePositives();

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
        findSampleContents().forEach(value -> {
            for (WordAbstractFinder finder : finders) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < ITERATIONS; i++) {
                    finder.findMatches(value);
                }
                long end = System.currentTimeMillis() - start;
                System.out.println(finder.getClass().getSimpleName() + "\t" + end);
            }
        });

        Assert.assertTrue(true);
    }

}
