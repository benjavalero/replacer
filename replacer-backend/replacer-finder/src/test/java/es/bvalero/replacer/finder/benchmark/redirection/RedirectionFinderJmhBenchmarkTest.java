package es.bvalero.replacer.finder.benchmark.redirection;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.immutable.finders.ImmutableFinderJmhBenchmarkTest;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class RedirectionFinderJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "redirection/redirection-summary-jmh";

    private ConfigurableApplicationContext context;

    private RedirectionLowercaseContainsFinder redirectionLowercaseContainsFinder;
    private RedirectionRegexFinder redirectionRegexFinder;
    private RedirectionRegexInsensitiveFinder redirectionRegexInsensitiveFinder;
    private RedirectionAutomatonFinder redirectionAutomatonFinder;
    private RedirectionAhoCorasickFinder redirectionAhoCorasickFinder;
    private RedirectionAhoCorasickLongestFinder redirectionAhoCorasickLongestFinder;
    private RedirectionAhoCorasickWholeFinder redirectionAhoCorasickWholeFinder;
    private RedirectionAhoCorasickWholeLongestFinder redirectionAhoCorasickWholeLongestFinder;

    @Setup
    public void setUp() throws WikipediaException {
        // Base set-up
        super.setUp();

        context = SpringApplication.run(ImmutableFinderJmhBenchmarkTest.class);
        context.registerShutdownHook();

        FinderProperties finderProperties = context.getBean(FinderProperties.class);

        // Initialize the finders
        List<String> redirectionWords = finderProperties.getRedirectionWords();
        redirectionLowercaseContainsFinder = new RedirectionLowercaseContainsFinder(redirectionWords);
        redirectionRegexFinder = new RedirectionRegexFinder(redirectionWords);
        redirectionRegexInsensitiveFinder = new RedirectionRegexInsensitiveFinder(redirectionWords);
        redirectionAutomatonFinder = new RedirectionAutomatonFinder(redirectionWords);
        redirectionAhoCorasickFinder = new RedirectionAhoCorasickFinder(redirectionWords);
        redirectionAhoCorasickLongestFinder = new RedirectionAhoCorasickLongestFinder(redirectionWords);
        redirectionAhoCorasickWholeFinder = new RedirectionAhoCorasickWholeFinder(redirectionWords);
        redirectionAhoCorasickWholeLongestFinder = new RedirectionAhoCorasickWholeLongestFinder(redirectionWords);
    }

    @Benchmark
    public void redirectionLowercaseContainsFinder(Blackhole bh) {
        runFinder(redirectionLowercaseContainsFinder, bh);
    }

    @Benchmark
    public void setRedirectionRegexFinder(Blackhole bh) {
        runFinder(redirectionRegexFinder, bh);
    }

    @Benchmark
    public void setRedirectionRegexInsensitiveFinder(Blackhole bh) {
        runFinder(redirectionRegexInsensitiveFinder, bh);
    }

    @Benchmark
    public void setRedirectionAutomatonFinder(Blackhole bh) {
        runFinder(redirectionAutomatonFinder, bh);
    }

    @Benchmark
    public void setRedirectionAhoCorasickFinder(Blackhole bh) {
        runFinder(redirectionAhoCorasickFinder, bh);
    }

    @Benchmark
    public void setRedirectionAhoCorasickLongestFinder(Blackhole bh) {
        runFinder(redirectionAhoCorasickLongestFinder, bh);
    }

    @Benchmark
    public void setRedirectionAhoCorasickWholeFinder(Blackhole bh) {
        runFinder(redirectionAhoCorasickWholeFinder, bh);
    }

    @Benchmark
    public void setRedirectionAhoCorasickWholeLongestFinder(Blackhole bh) {
        runFinder(redirectionAhoCorasickWholeLongestFinder, bh);
    }

    @Test
    void testBenchmark() throws RunnerException {
        run(RedirectionFinderJmhBenchmarkTest.class, fileName);

        assertTrue(true);
    }
}
