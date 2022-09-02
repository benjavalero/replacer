package es.bvalero.replacer.finder.benchmark.simple;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SimpleFinderBenchmarkTest extends BaseFinderBenchmark {

    private static final String fileName = "simple/simple-benchmark.csv";

    @Test
    void testBenchmark() throws ReplacerException {
        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new SimpleRegexFinder());
        finders.add(new SimpleAutomatonFinder());
        finders.add(new SimpleLinearFinder());

        runBenchmark(finders, fileName);

        assertTrue(true);
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        generateBoxplot(fileName, "Simple");
    }
}
