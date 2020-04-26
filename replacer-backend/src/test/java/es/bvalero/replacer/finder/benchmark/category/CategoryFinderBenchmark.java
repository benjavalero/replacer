package es.bvalero.replacer.finder.benchmark.category;

import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CategoryFinderBenchmark extends BaseFinderBenchmark {

    @Test
    public void testBenchmark() throws IOException, URISyntaxException {
        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new CategoryRegexFinder());
        finders.add(new CategoryAutomatonFinder());
        finders.add(new CategoryLinearFinder());

        runBenchmark(finders);

        Assertions.assertTrue(true);
    }
}
