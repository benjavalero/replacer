package es.bvalero.replacer.benchmark;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class CursiveFinderBenchmark extends BaseFinderBenchmark {

    private static final int ITERATIONS = 1000;

    @Test
    public void testBenchmark() throws IOException, URISyntaxException {
        // Load the finders
        List<CursiveAbstractFinder> finders = new ArrayList<>();
        finders.add(new CursiveRegexDotLazyFinder());
        finders.add(new CursiveRegexFinder());
        finders.add(new CursiveRegexDotAllLookFinder());
        finders.add(new CursiveRegexLookFinder());
        finders.add(new CursiveAutomatonFinder());

        System.out.println();
        System.out.println("FINDER\tTIME");
        findSampleContents().forEach(value -> {
            for (CursiveAbstractFinder finder : finders) {
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
