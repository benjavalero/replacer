package es.bvalero.replacer.finder.benchmark.cursive;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

public class CursiveFinderJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "cursive/cursive-summary-jmh";

    private CursiveRegexDotLazyFinder cursiveRegexDotLazyFinder;
    private CursiveRegexFinder cursiveRegexFinder;
    private CursiveRegexDotAllLookFinder cursiveRegexDotAllLookFinder;
    private CursiveRegexLookFinder cursiveRegexLookFinder;
    private CursiveAutomatonFinder cursiveAutomatonFinder;
    private CursiveLinearFinder cursiveLinearFinder;
    private CursiveFinalFinder cursiveFinalFinder;

    @Override
    @Setup
    public void setUp() throws ReplacerException {
        // Base set-up
        super.setUp();

        // Initialize the finders
        cursiveRegexDotLazyFinder = new CursiveRegexDotLazyFinder();
        cursiveRegexFinder = new CursiveRegexFinder();
        cursiveRegexDotAllLookFinder = new CursiveRegexDotAllLookFinder();
        cursiveRegexLookFinder = new CursiveRegexLookFinder();
        cursiveAutomatonFinder = new CursiveAutomatonFinder();
        cursiveLinearFinder = new CursiveLinearFinder();
        cursiveFinalFinder = new CursiveFinalFinder();
    }

    @Benchmark
    public void cursiveRegexDotLazyFinder(Blackhole bh) {
        runFinder(cursiveRegexDotLazyFinder, bh);
    }

    @Benchmark
    public void cursiveRegexFinder(Blackhole bh) {
        runFinder(cursiveRegexFinder, bh);
    }

    @Benchmark
    public void cursiveRegexDotAllLookFinder(Blackhole bh) {
        runFinder(cursiveRegexDotAllLookFinder, bh);
    }

    @Benchmark
    public void cursiveRegexLookFinder(Blackhole bh) {
        runFinder(cursiveRegexLookFinder, bh);
    }

    @Benchmark
    public void cursiveAutomatonFinder(Blackhole bh) {
        runFinder(cursiveAutomatonFinder, bh);
    }

    @Benchmark
    public void cursiveLinearFinder(Blackhole bh) {
        runFinder(cursiveLinearFinder, bh);
    }

    @Benchmark
    public void cursiveFinalFinder(Blackhole bh) {
        runFinder(cursiveFinalFinder, bh);
    }

    @Test
    void testGenerateChartBoxplot() throws ReplacerException {
        generateChart(fileName);
        assertTrue(true);
    }

    public static void main(String[] args) throws RunnerException {
        run(CursiveFinderJmhBenchmarkTest.class, fileName);
    }
}
