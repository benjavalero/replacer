package es.bvalero.replacer.finder.benchmark;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CategoryFinderBenchmark extends BaseFinderBenchmark {
    private static final int ITERATIONS = 1000;

    @Test
    public void testBenchmark() throws IOException, URISyntaxException {
        // Load the finders
        List<CategoryAbstractFinder> finders = new ArrayList<>();
        finders.add(new CategoryRegexFinder());
        finders.add(new CategoryRegexClassFinder());
        finders.add(new CategoryRegexClassLazyFinder());
        finders.add(new CategoryRegexClassPossessiveFinder());
        finders.add(new CategoryAutomatonFinder());

        System.out.println();
        System.out.println("FINDER\tTIME");
        findSampleContents()
            .forEach(
                value -> {
                    for (CategoryAbstractFinder finder : finders) {
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
