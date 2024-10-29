package es.bvalero.replacer.finder.benchmark.completetag;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.immutable.finders.ImmutableFinderJmhBenchmarkTest;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class CompleteTagFinderJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "completetag/complete-tag-summary-jmh";

    private ConfigurableApplicationContext context;

    private CompleteTagRegexIteratedFinder completeTagRegexIteratedFinder;
    private CompleteTagRegexBackReferenceFinder completeTagRegexBackReferenceFinder;
    private CompleteTagLinearIteratedFinder completeTagLinearIteratedFinder;
    private CompleteTagLinearFinder completeTagLinearFinder;
    private CompleteTagFinalFinder completeTagFinalFinder;

    @Setup
    public void setUp() throws WikipediaException {
        // Base set-up
        super.setUp();

        context = SpringApplication.run(ImmutableFinderJmhBenchmarkTest.class);
        context.registerShutdownHook();

        FinderProperties finderProperties = context.getBean(FinderProperties.class);

        // Initialize the finders
        Set<String> completeTags = finderProperties.getCompleteTags();
        completeTagRegexIteratedFinder = new CompleteTagRegexIteratedFinder(completeTags);
        completeTagRegexBackReferenceFinder = new CompleteTagRegexBackReferenceFinder(completeTags);
        completeTagLinearIteratedFinder = new CompleteTagLinearIteratedFinder(completeTags);
        completeTagLinearFinder = new CompleteTagLinearFinder(completeTags);
        completeTagFinalFinder = new CompleteTagFinalFinder(completeTags);
    }

    @Benchmark
    public void completeTagRegexIteratedFinder(Blackhole bh) {
        runFinder(completeTagRegexIteratedFinder, bh);
    }

    @Benchmark
    public void completeTagRegexBackReferenceFinder(Blackhole bh) {
        runFinder(completeTagRegexBackReferenceFinder, bh);
    }

    @Benchmark
    public void completeTagLinearIteratedFinder(Blackhole bh) {
        runFinder(completeTagLinearIteratedFinder, bh);
    }

    @Benchmark
    public void completeTagLinearFinder(Blackhole bh) {
        runFinder(completeTagLinearFinder, bh);
    }

    @Benchmark
    public void completeTagFinalFinder(Blackhole bh) {
        runFinder(completeTagFinalFinder, bh);
    }

    @Test
    void testBenchmark() throws RunnerException {
        run(CompleteTagFinderJmhBenchmarkTest.class, fileName);

        assertTrue(true);
    }
}
