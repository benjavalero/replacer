package es.bvalero.replacer.finder.benchmark.simple;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.wikipedia.WikipediaException;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

public class SimpleFinderJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "simple/simple-summary-jmh";

    private SimpleLinearFinder simpleLinearFinder;
    private SimpleRegexFinder simpleRegexFinder;
    private SimpleAutomatonFinder simpleAutomatonFinder;

    @Setup
    public void setUp() throws WikipediaException {
        // Base set-up
        super.setUp();

        // Initialize the finders
        final String word = "_";
        simpleLinearFinder = new SimpleLinearFinder(word);
        simpleRegexFinder = new SimpleRegexFinder(word);
        simpleAutomatonFinder = new SimpleAutomatonFinder(word);
    }

    @Benchmark
    public void simpleLinearFinder(Blackhole bh) {
        runFinder(simpleLinearFinder, bh);
    }

    @Benchmark
    public void simpleRegexFinder(Blackhole bh) {
        runFinder(simpleRegexFinder, bh);
    }

    @Benchmark
    public void simpleAutomatonFinder(Blackhole bh) {
        runFinder(simpleAutomatonFinder, bh);
    }

    @Test
    void testBenchmark() throws RunnerException {
        run(SimpleFinderJmhBenchmarkTest.class, fileName);

        assertTrue(true);
    }
}
