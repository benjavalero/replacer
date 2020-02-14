package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.misspelling.MisspellingManager;
import es.bvalero.replacer.finder.misspelling.UppercaseAfterFinder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(
    classes = { UppercaseAfterFinder.class, MisspellingManager.class },
    initializers = ConfigFileApplicationContextInitializer.class
)
public class UppercaseFinderBenchmark extends BaseFinderBenchmark {
    private static final int ITERATIONS = 1000;

    @Autowired
    private MisspellingManager misspellingManager;

    @Autowired
    private UppercaseAfterFinder uppercaseAfterFinder;

    @Test
    public void testBenchmark() throws IOException, URISyntaxException {
        // Load the misspellings
        misspellingManager.update();
        Collection<String> words = uppercaseAfterFinder.getUppercaseWords();

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
        findSampleContents()
            .forEach(
                value -> {
                    for (UppercaseAbstractFinder finder : finders) {
                        long start = System.currentTimeMillis();
                        for (int i = 0; i < ITERATIONS; i++) {
                            finder.findMatches(value);
                        }
                        long end = System.currentTimeMillis() - start;
                        System.out.println(finder.getClass().getSimpleName() + "\t" + end);
                    }
                }
            );

        Assert.assertTrue(true);
    }
}
