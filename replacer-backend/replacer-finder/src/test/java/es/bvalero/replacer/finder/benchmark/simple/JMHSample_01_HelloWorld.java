package es.bvalero.replacer.finder.benchmark.simple;

import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.benchmark.util.BenchmarkUtils;
import es.bvalero.replacer.page.find.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Fork(1)
@State(Scope.Benchmark)
@Warmup(time = 1)
@Measurement(time = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class JMHSample_01_HelloWorld {

    private List<WikipediaPage> sampleContents;

    private SimpleLinearFinder simpleLinearFinder;
    private SimpleRegexFinder simpleRegexFinder;
    private SimpleAutomatonFinder simpleAutomatonFinder;

    @Setup
    public void setUp() throws WikipediaException {
        // The pages have been sampled so the distribution of their content lengths
        // match the one of the content lengths of all indexable pages
        sampleContents = BenchmarkUtils.findSampleContents();

        // Initialize the finders
        final String word = "_";
        simpleLinearFinder = new SimpleLinearFinder(word);
        simpleRegexFinder = new SimpleRegexFinder(word);
        simpleAutomatonFinder = new SimpleAutomatonFinder(word);
    }

    private void runFinder(Finder<?> finder, Blackhole bh) {
        // As the sample represents the whole dump,
        // finding all over the sample represents the time finding all over the dump.
        // NOTE: therefore, the average time corresponds to run the finder in the 50 sample pages.
        sampleContents.forEach(page -> finder.find(page).forEach(bh::consume));
    }

    @Benchmark
    public void baseline() {
        // Do nothing
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

    public static void main(String[] args) throws RunnerException, IOException, URISyntaxException, WikipediaException {
        final String testResourcesPath = "replacer-finder/src/test/resources/es/bvalero/replacer/finder/benchmark/";
        final String fileName = "simple/simple-summary-jmh";
        Options opt = new OptionsBuilder()
            .include(JMHSample_01_HelloWorld.class.getSimpleName())
            .resultFormat(ResultFormatType.TEXT)
            .result(testResourcesPath + fileName + ".txt")
            .build();

        new Runner(opt).run();
    }
}
