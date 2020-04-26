package es.bvalero.replacer.finder.benchmark.filename;

import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FileFinderBenchmark extends BaseFinderBenchmark {

    @Test
    public void testBenchmark() throws IOException, URISyntaxException {
        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new FileRegexFinder());
        finders.add(new FileRegexGroupFinder());
        finders.add(new FileAutomatonFinder());
        finders.add(new FileLinearFinder());

        runBenchmark(finders);

        Assertions.assertTrue(true);
    }
}
