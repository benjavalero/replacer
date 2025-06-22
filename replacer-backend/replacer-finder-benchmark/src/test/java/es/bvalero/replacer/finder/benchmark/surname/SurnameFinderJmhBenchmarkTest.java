package es.bvalero.replacer.finder.benchmark.surname;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@EnableConfigurationProperties(FinderProperties.class)
public class SurnameFinderJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "surname/surname-summary-jmh";

    private ConfigurableApplicationContext context;

    private SurnameIndexOfFinder surnameIndexOfFinder;
    private SurnameRegexFinder surnameRegexFinder;
    private SurnameAutomatonFinder surnameAutomatonFinder;
    private SurnameRegexCompleteFinder surnameRegexCompleteFinder;
    private SurnameAutomatonCompleteFinder surnameAutomatonCompleteFinder;
    private SurnameRegexAlternateFinder surnameRegexAlternateFinder;
    private SurnameAutomatonAlternateFinder surnameAutomatonAlternateFinder;
    private SurnameRegexAlternateCompleteFinder surnameRegexAlternateCompleteFinder;
    private SurnameAutomatonAlternateCompleteFinder surnameAutomatonAlternateCompleteFinder;
    private SurnameAhoCorasickFinder surnameAhoCorasickFinder;
    private SurnameAhoCorasickLongestFinder surnameAhoCorasickLongestFinder;
    private SurnameAhoCorasickWholeLongestFinder surnameAhoCorasickWholeLongestFinder;

    @Setup
    public void setUp() throws ReplacerException {
        // Base set-up
        super.setUp();

        context = SpringApplication.run(SurnameFinderJmhBenchmarkTest.class);
        context.registerShutdownHook();

        FinderProperties finderProperties = context.getBean(FinderProperties.class);

        // Initialize the finders
        Set<String> personSurnames = finderProperties
            .getPersonSurnames()
            .stream()
            .map(FinderProperties.PersonSurname::getSurname)
            .collect(Collectors.toUnmodifiableSet());
        surnameIndexOfFinder = new SurnameIndexOfFinder(personSurnames);
        surnameRegexFinder = new SurnameRegexFinder(personSurnames);
        surnameAutomatonFinder = new SurnameAutomatonFinder(personSurnames);
        surnameRegexCompleteFinder = new SurnameRegexCompleteFinder(personSurnames);
        surnameAutomatonCompleteFinder = new SurnameAutomatonCompleteFinder(personSurnames);
        surnameRegexAlternateFinder = new SurnameRegexAlternateFinder(personSurnames);
        surnameAutomatonAlternateFinder = new SurnameAutomatonAlternateFinder(personSurnames);
        surnameRegexAlternateCompleteFinder = new SurnameRegexAlternateCompleteFinder(personSurnames);
        surnameAutomatonAlternateCompleteFinder = new SurnameAutomatonAlternateCompleteFinder(personSurnames);
        surnameAhoCorasickFinder = new SurnameAhoCorasickFinder(personSurnames);
        surnameAhoCorasickLongestFinder = new SurnameAhoCorasickLongestFinder(personSurnames);
        surnameAhoCorasickWholeLongestFinder = new SurnameAhoCorasickWholeLongestFinder(personSurnames);
    }

    @Benchmark
    public void surnameIndexOfFinder(Blackhole bh) {
        runFinder(surnameIndexOfFinder, bh);
    }

    @Benchmark
    public void surnameRegexFinder(Blackhole bh) {
        runFinder(surnameRegexFinder, bh);
    }

    @Benchmark
    public void surnameAutomatonFinder(Blackhole bh) {
        runFinder(surnameAutomatonFinder, bh);
    }

    @Benchmark
    public void surnameRegexCompleteFinder(Blackhole bh) {
        runFinder(surnameRegexCompleteFinder, bh);
    }

    @Benchmark
    public void surnameAutomatonCompleteFinder(Blackhole bh) {
        runFinder(surnameAutomatonCompleteFinder, bh);
    }

    @Benchmark
    public void surnameRegexAlternateFinder(Blackhole bh) {
        runFinder(surnameRegexAlternateFinder, bh);
    }

    @Benchmark
    public void surnameAutomatonAlternateFinder(Blackhole bh) {
        runFinder(surnameAutomatonAlternateFinder, bh);
    }

    @Benchmark
    public void surnameRegexAlternateCompleteFinder(Blackhole bh) {
        runFinder(surnameRegexAlternateCompleteFinder, bh);
    }

    @Benchmark
    public void surnameAutomatonAlternateCompleteFinder(Blackhole bh) {
        runFinder(surnameAutomatonAlternateCompleteFinder, bh);
    }

    @Benchmark
    public void surnameAhoCorasickFinder(Blackhole bh) {
        runFinder(surnameAhoCorasickFinder, bh);
    }

    @Benchmark
    public void surnameAhoCorasickLongestFinder(Blackhole bh) {
        runFinder(surnameAhoCorasickLongestFinder, bh);
    }

    @Benchmark
    public void surnameAhoCorasickWholeLongestFinder(Blackhole bh) {
        runFinder(surnameAhoCorasickWholeLongestFinder, bh);
    }

    @Test
    void testGenerateChartBoxplot() throws ReplacerException {
        generateChart(fileName);
        assertTrue(true);
    }

    public static void main(String[] args) throws RunnerException {
        run(SurnameFinderJmhBenchmarkTest.class, fileName);
    }
}
