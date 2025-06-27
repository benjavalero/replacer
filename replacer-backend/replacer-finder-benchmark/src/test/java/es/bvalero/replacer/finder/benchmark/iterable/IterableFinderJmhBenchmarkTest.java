package es.bvalero.replacer.finder.benchmark.iterable;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.regex.MatchResult;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.springframework.lang.Nullable;

@Warmup(time = 5) // Default: 5 iterations, 10 s each
@Measurement(time = 5) // Default: 5 iterations, 10 s each
public class IterableFinderJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "iterable/iterable-summary-jmh";
    private static final String word = "_";

    @Setup
    public void setUp() throws ReplacerException {
        // Base set-up
        super.setUp();
    }

    @Benchmark
    public void linearFinder(Blackhole bh) {
        sampleContents.forEach(page -> LinearMatchFinder.find(page, this::findMatch).forEach(bh::consume));
    }

    @Benchmark
    public void streamFinder(Blackhole bh) {
        sampleContents.forEach(page -> StreamMatchFinder.find(page, this::findMatch).forEach(bh::consume));
    }

    @Benchmark
    public void listFinder(Blackhole bh) {
        sampleContents.forEach(page -> ListMatchFinder.find(page, this::findMatch).forEach(bh::consume));
    }

    @Nullable
    private MatchResult findMatch(FinderPage page, int start) {
        final String text = page.getContent();
        if (start >= 0 && start < text.length()) {
            final int startMatch = findStartMatch(text, start);
            if (startMatch >= 0) {
                return FinderMatchResult.of(startMatch, word);
            } else {
                return null;
            }
        }
        return null;
    }

    private int findStartMatch(String text, int start) {
        return text.indexOf(word, start);
    }

    @Test
    void testGenerateChartBoxplot() throws ReplacerException {
        generateChart(fileName);
        assertTrue(true);
    }

    public static void main(String[] args) throws RunnerException {
        run(IterableFinderJmhBenchmarkTest.class, fileName);
    }
}
