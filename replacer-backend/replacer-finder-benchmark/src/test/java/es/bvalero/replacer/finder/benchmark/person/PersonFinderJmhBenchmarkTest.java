package es.bvalero.replacer.finder.benchmark.person;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@EnableConfigurationProperties(FinderProperties.class)
public class PersonFinderJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "person/person-summary-jmh";

    private ConfigurableApplicationContext context;

    private PersonIndexOfFinder personIndexOfFinder;
    private PersonRegexFinder personRegexFinder;
    private PersonAutomatonFinder personAutomatonFinder;
    private PersonRegexCompleteFinder personRegexCompleteFinder;
    private PersonAutomatonCompleteFinder personAutomatonCompleteFinder;
    private PersonRegexAlternateFinder personRegexAlternateFinder;
    private PersonAutomatonAlternateFinder personAutomatonAlternateFinder;
    private PersonRegexAlternateCompleteFinder personRegexAlternateCompleteFinder;
    private PersonAutomatonAlternateCompleteFinder personAutomatonAlternateCompleteFinder;
    private PersonAhoCorasickFinder personAhoCorasickFinder;
    private PersonAhoCorasickLongestFinder personAhoCorasickLongestFinder;
    private PersonAhoCorasickWholeLongestFinder personAhoCorasickWholeLongestFinder;

    @Override
    @Setup
    public void setUp() throws ReplacerException {
        // Base set-up
        super.setUp();

        context = SpringApplication.run(PersonFinderJmhBenchmarkTest.class);
        context.registerShutdownHook();

        FinderProperties finderProperties = context.getBean(FinderProperties.class);

        // Initialize the finders
        Set<String> personNames = new HashSet<>(finderProperties.getPersonNames());
        personIndexOfFinder = new PersonIndexOfFinder(personNames);
        personRegexFinder = new PersonRegexFinder(personNames);
        personAutomatonFinder = new PersonAutomatonFinder(personNames);
        personRegexCompleteFinder = new PersonRegexCompleteFinder(personNames);
        personAutomatonCompleteFinder = new PersonAutomatonCompleteFinder(personNames);
        personRegexAlternateFinder = new PersonRegexAlternateFinder(personNames);
        personAutomatonAlternateFinder = new PersonAutomatonAlternateFinder(personNames);
        personRegexAlternateCompleteFinder = new PersonRegexAlternateCompleteFinder(personNames);
        personAutomatonAlternateCompleteFinder = new PersonAutomatonAlternateCompleteFinder(personNames);
        personAhoCorasickFinder = new PersonAhoCorasickFinder(personNames);
        personAhoCorasickLongestFinder = new PersonAhoCorasickLongestFinder(personNames);
        personAhoCorasickWholeLongestFinder = new PersonAhoCorasickWholeLongestFinder(personNames);
    }

    @Benchmark
    public void personIndexOfFinder(Blackhole bh) {
        runFinder(personIndexOfFinder, bh);
    }

    @Benchmark
    public void personRegexFinder(Blackhole bh) {
        runFinder(personRegexFinder, bh);
    }

    @Benchmark
    public void personAutomatonFinder(Blackhole bh) {
        runFinder(personAutomatonFinder, bh);
    }

    @Benchmark
    public void personRegexCompleteFinder(Blackhole bh) {
        runFinder(personRegexCompleteFinder, bh);
    }

    @Benchmark
    public void personAutomatonCompleteFinder(Blackhole bh) {
        runFinder(personAutomatonCompleteFinder, bh);
    }

    @Benchmark
    public void personRegexAlternateFinder(Blackhole bh) {
        runFinder(personRegexAlternateFinder, bh);
    }

    @Benchmark
    public void personAutomatonAlternateFinder(Blackhole bh) {
        runFinder(personAutomatonAlternateFinder, bh);
    }

    @Benchmark
    public void personRegexAlternateCompleteFinder(Blackhole bh) {
        runFinder(personRegexAlternateCompleteFinder, bh);
    }

    @Benchmark
    public void personAutomatonAlternateCompleteFinder(Blackhole bh) {
        runFinder(personAutomatonAlternateCompleteFinder, bh);
    }

    @Benchmark
    public void personAhoCorasickFinder(Blackhole bh) {
        runFinder(personAhoCorasickFinder, bh);
    }

    @Benchmark
    public void personAhoCorasickLongestFinder(Blackhole bh) {
        runFinder(personAhoCorasickLongestFinder, bh);
    }

    @Benchmark
    public void personAhoCorasickWholeLongestFinder(Blackhole bh) {
        runFinder(personAhoCorasickWholeLongestFinder, bh);
    }

    @Test
    void testGenerateChartBoxplot() throws ReplacerException {
        generateChart(fileName);
        assertTrue(true);
    }

    public static void main(String[] args) throws RunnerException {
        run(PersonFinderJmhBenchmarkTest.class, fileName);
    }
}
