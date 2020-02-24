package es.bvalero.replacer.finder.benchmark.domain;

import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class DomainFinderBenchmark extends BaseFinderBenchmark {
    private static final int ITERATIONS = 1000;

    @Test
    public void testBenchmark() throws IOException, URISyntaxException {
        // Load the finders
        // In order to capture nested tags we can only use lazy regex
        List<DomainFinder> finders = new ArrayList<>();
        finders.add(new DomainAutomatonFinder());
        finders.add(new DomainAutomatonPrefixFinder());
        finders.add(new DomainAutomatonSuffixFinder());
        finders.add(new DomainAutomatonPrefixSuffixFinder());
        finders.add(new DomainLinearFinder());
        finders.add(new DomainLinearSuffixFinder());
        finders.add(new DomainLinearSuffixListFinder());

        System.out.println();
        System.out.println("FINDER\tTIME");
        findSampleContents()
            .forEach(
                value -> {
                    for (DomainFinder finder : finders) {
                        long start = System.currentTimeMillis();
                        for (int i = 0; i < ITERATIONS; i++) {
                            finder.findMatches(value).size();
                        }
                        long end = System.currentTimeMillis() - start;
                        System.out.println(finder.getClass().getSimpleName() + "\t" + end);
                    }
                }
            );

        Assert.assertTrue(true);
    }
}
