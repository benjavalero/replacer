package es.bvalero.replacer.finder.benchmark.redirection;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import java.util.List;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@EnableConfigurationProperties(FinderProperties.class)
public class RedirectionFinderJmhBenchmark extends BaseFinderJmhBenchmark {

    private static final String fileName = "redirection/redirection-summary-jmh";

    private ConfigurableApplicationContext context;

    private RedirectionLowercaseContainsFinder redirectionLowercaseContainsFinder;
    private RedirectionLowercasePrefixFinder redirectionLowercasePrefixFinder;
    // private RedirectionRegexFinder redirectionRegexFinder; // Very long
    // private RedirectionRegexInsensitiveFinder redirectionRegexInsensitiveFinder; // Very long
    // private RedirectionAutomatonFinder redirectionAutomatonFinder; // Medium
    private RedirectionAhoCorasickFinder redirectionAhoCorasickFinder; // Medium
    // private RedirectionAhoCorasickLongestFinder redirectionAhoCorasickLongestFinder; // Medium
    private RedirectionAhoCorasickWholeFinder redirectionAhoCorasickWholeFinder;

    // private RedirectionAhoCorasickWholeLongestFinder redirectionAhoCorasickWholeLongestFinder; // Medium

    // private RedirectionLowercaseAhoCorasickFinder redirectionLowercaseAhoCorasickFinder; // Worse than case-insensitive
    // private RedirectionLowercaseAhoCorasickLongestFinder redirectionLowercaseAhoCorasickLongestFinder; // Worse than case-insensitive
    // private RedirectionLowercaseAhoCorasickWholeFinder redirectionLowercaseAhoCorasickWholeFinder; // Worse than case-insensitive
    // private RedirectionLowercaseAhoCorasickWholeLongestFinder redirectionLowercaseAhoCorasickWholeLongestFinder; // Worse than case-insensitive

    @Override
    @Setup
    public void setUp() throws ReplacerException {
        // Base set-up
        super.setUp();

        context = SpringApplication.run(RedirectionFinderJmhBenchmark.class);
        context.registerShutdownHook();

        FinderProperties finderProperties = context.getBean(FinderProperties.class);

        // Initialize the finders
        List<String> redirectionWords = finderProperties.getRedirectionWords();
        redirectionLowercaseContainsFinder = new RedirectionLowercaseContainsFinder(redirectionWords);
        redirectionLowercasePrefixFinder = new RedirectionLowercasePrefixFinder(redirectionWords);
        // redirectionRegexFinder = new RedirectionRegexFinder(redirectionWords);
        // redirectionRegexInsensitiveFinder = new RedirectionRegexInsensitiveFinder(redirectionWords);
        // redirectionAutomatonFinder = new RedirectionAutomatonFinder(redirectionWords);
        redirectionAhoCorasickFinder = new RedirectionAhoCorasickFinder(redirectionWords);
        // redirectionAhoCorasickLongestFinder = new RedirectionAhoCorasickLongestFinder(redirectionWords);
        redirectionAhoCorasickWholeFinder = new RedirectionAhoCorasickWholeFinder(redirectionWords);
        // redirectionAhoCorasickWholeLongestFinder = new RedirectionAhoCorasickWholeLongestFinder(redirectionWords);
        // redirectionLowercaseAhoCorasickFinder = new RedirectionLowercaseAhoCorasickFinder(redirectionWords);
        // redirectionLowercaseAhoCorasickLongestFinder = new RedirectionLowercaseAhoCorasickLongestFinder(redirectionWords);
        // redirectionLowercaseAhoCorasickWholeFinder = new RedirectionLowercaseAhoCorasickWholeFinder(redirectionWords);
        // redirectionLowercaseAhoCorasickWholeLongestFinder = new RedirectionLowercaseAhoCorasickWholeLongestFinder(redirectionWords);
    }

    @Benchmark
    public void redirectionLowercaseContainsFinder(Blackhole bh) {
        runFinder(redirectionLowercaseContainsFinder, bh);
    }

    @Benchmark
    public void redirectionLowercasePrefixFinder(Blackhole bh) {
        runFinder(redirectionLowercasePrefixFinder, bh);
    }

    /*
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
    */

    @Benchmark
    public void setRedirectionAhoCorasickFinder(Blackhole bh) {
        runFinder(redirectionAhoCorasickFinder, bh);
    }

    /*
    @Benchmark
    public void setRedirectionAhoCorasickLongestFinder(Blackhole bh) {
        runFinder(redirectionAhoCorasickLongestFinder, bh);
    }
    */

    @Benchmark
    public void setRedirectionAhoCorasickWholeFinder(Blackhole bh) {
        runFinder(redirectionAhoCorasickWholeFinder, bh);
    }

    /*
    @Benchmark
    public void setRedirectionAhoCorasickWholeLongestFinder(Blackhole bh) {
        runFinder(redirectionAhoCorasickWholeLongestFinder, bh);
    }
    */

    /*
    @Benchmark
    public void setRedirectionLowercaseAhoCorasickFinder(Blackhole bh) {
        runFinder(redirectionLowercaseAhoCorasickFinder, bh);
    }

    @Benchmark
    public void setRedirectionLowercaseAhoCorasickLongestFinder(Blackhole bh) {
        runFinder(redirectionLowercaseAhoCorasickLongestFinder, bh);
    }

    @Benchmark
    public void setRedirectionLowercaseAhoCorasickWholeFinder(Blackhole bh) {
        runFinder(redirectionLowercaseAhoCorasickWholeFinder, bh);
    }

    @Benchmark
    public void setRedirectionLowercaseAhoCorasickWholeLongestFinder(Blackhole bh) {
        runFinder(redirectionLowercaseAhoCorasickWholeLongestFinder, bh);
    }
    */

    public static void main(String[] args) throws RunnerException, ReplacerException {
        // run(RedirectionFinderJmhBenchmark.class, fileName);

        generateChart(fileName);
    }
}
