package es.bvalero.replacer.benchmark;

import es.bvalero.replacer.finder.misspelling.MisspellingFinder;
import es.bvalero.replacer.finder.misspelling.MisspellingManager;
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
@ContextConfiguration(classes = {MisspellingFinder.class, MisspellingManager.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class WordFinderBenchmark extends BaseFinderBenchmark {

    private static final int ITERATIONS = 1000;

    @Autowired
    private MisspellingManager misspellingManager;

    @Autowired
    private MisspellingFinder misspellingFinder;

    @Test
    public void testWordFinderBenchmark() throws IOException, URISyntaxException {
        // Load the misspellings
        misspellingManager.update();
        Collection<String> words = misspellingFinder.getMisspellingMap().keySet();

        // Load the finders
        List<WordAbstractFinder> finders = new ArrayList<>();
        /*
        // These finders are discarded as the times are about 100x slower compared with the other strategy
        finders.add(new WordIndexOfFinder(this.words));
        finders.add(new WordRegexFinder(this.words));
        // finders.add(new WordAutomatonFinder(this.words)); // Discarded: we need to increase too much the heap size
        finders.add(new WordRegexCompleteFinder(this.words));
        finders.add(new WordAlternateRegexFinder(this.words));
        // finders.add(new WordAlternateAutomatonFinder(this.words)); // Discarded: we need to increase too much the stack size
        finders.add(new WordAlternateRegexCompleteFinder(this.words));
        */
        finders.add(new WordRegexAllFinder(words));
        finders.add(new WordAutomatonAllFinder(words)); // WINNER
        finders.add(new WordRegexAllPossessiveFinder(words));
        finders.add(new WordRegexAllCompleteFinder(words));
        finders.add(new WordRegexAllCompletePossessiveFinder(words));

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
