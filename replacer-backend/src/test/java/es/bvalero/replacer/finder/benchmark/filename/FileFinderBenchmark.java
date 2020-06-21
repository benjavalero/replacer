package es.bvalero.replacer.finder.benchmark.filename;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

public class FileFinderBenchmark extends BaseFinderBenchmark {

    @Test
    void testBenchmark() throws IOException, URISyntaxException {
        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new FileRegexFinder());
        finders.add(new FileRegexGroupFinder());
        finders.add(new FileAutomatonFinder());
        finders.add(new FileLinearFinder());

        runBenchmark(finders);

        MatcherAssert.assertThat(true, is(true));
    }
}
