package es.bvalero.replacer.benchmark;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ParameterValueFinderBenchmark extends BaseFinderBenchmark {

    private static final int ITERATIONS = 1000;

    @Test
    public void testBenchmark() throws IOException, URISyntaxException {
        // Load the finders
        List<ParameterValueAbstractFinder> finders = new ArrayList<>();
        finders.add(new ParameterValueRegexFinder());
        finders.add(new ParameterValueRegexPossessiveFinder());
        finders.add(new ParameterValueRegexNoGroupFinder());
        finders.add(new ParameterValueAutomatonFinder());

        System.out.println();
        System.out.println("FINDER\tTIME");
        findSampleContents().forEach(value -> {
            for (ParameterValueAbstractFinder finder : finders) {
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
