package es.bvalero.replacer.benchmark;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PersonFinderBenchmark extends BaseFinderBenchmark {

    private static final int ITERATIONS = 1000;

    @Test
    public void testBenchmark() throws IOException, URISyntaxException {
        Collection<String> words = Arrays.asList("Domingo", "Frances", "Julio", "Sidney");

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
        findSampleContents().forEach(value -> {
            for (PersonAbstractFinder finder : finders) {
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
