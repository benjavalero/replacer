package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.benchmark.util.BenchmarkUtils;
import es.bvalero.replacer.page.find.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Fork(value = 1, jvmArgsAppend = { "-Xmx256m", "-da" }) // 0 makes debugging possible, disable assertions just in case
@State(Scope.Benchmark)
@Warmup(time = 2) // Default: 5 iterations, 10 s each
@Measurement(time = 2) // Default: 5 iterations, 10 s each
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class BaseFinderJmhBenchmark {

    private static final String TEST_RESOURCES_PATH =
        "replacer-backend/replacer-finder-benchmark/src/main/resources/es/bvalero/replacer/finder/benchmark/";

    protected List<WikipediaPage> sampleContents;
    private Map<Integer, Integer> samplePages;

    // @Param({"1", "2", "3"})
    public int pageId = 0;

    // Note: Timeout is 10 min per iteration by default.
    // However, it only works on the teardown phase,
    // so it is not useful to interrupt very long benchmarks.

    protected void setUp() throws WikipediaException {
        // The pages have been sampled so the distribution of their content lengths
        // match the one of the content lengths of all indexable pages
        sampleContents = BenchmarkUtils.findSampleContents();

        // We choose 3 sample pages to represent different sizes
        // 1: 8597062   // Small (2,5 kB)
        // 2: 811007    // Medium (6 kB)
        // 3: 7707926   // Large (14 kB)
        samplePages = Map.of(1, 8597062, 2, 811007, 3, 7707926);
    }

    protected void runFinder(Finder<?> finder, Blackhole bh) {
        if (pageId == 0) {
            // As the sample represents the whole dump,
            // finding all over the sample represents the time finding all over the dump.
            // NOTE: therefore, the average time corresponds to run the finder in the 50 sample pages.
            sampleContents.forEach(page -> finder.find(page).forEach(bh::consume));
        } else {
            sampleContents
                .stream()
                .filter(p -> p.getPageKey().getPageId() == samplePages.get(pageId))
                .forEach(page -> finder.find(page).forEach(bh::consume));
        }
    }

    @Benchmark
    public void baseLine() {
        // Do nothing
    }

    protected static void run(Class<? extends BaseFinderJmhBenchmark> jmhBenchmarkClass, String fileName)
        throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(jmhBenchmarkClass.getSimpleName())
            // .addProfiler(GCProfiler.class)
            // .addProfiler(MemPoolProfiler.class)
            .resultFormat(ResultFormatType.TEXT)
            .result(TEST_RESOURCES_PATH + fileName + ".txt")
            .build();

        new Runner(opt).run();
    }
}
