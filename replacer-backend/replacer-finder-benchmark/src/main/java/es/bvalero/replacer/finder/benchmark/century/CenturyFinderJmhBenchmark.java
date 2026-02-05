package es.bvalero.replacer.finder.benchmark.century;

import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.benchmark.BaselineFinder;
import es.bvalero.replacer.finder.replacement.finders.CenturyFinder;
import lombok.SneakyThrows;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Warmup(time = 5) // Default: 5 iterations, 10 s each
@Measurement(time = 5) // Default: 5 iterations, 10 s each
public class CenturyFinderJmhBenchmark extends BaseFinderJmhBenchmark {

    private static final String fileName = "century/century-summary-jmh";

    private BaselineFinder baselineFinder;
    private CenturyFinder centuryFinder;
    private CenturyNewFinder centuryNewFinder;

    @Override
    @Setup
    public void setUp() throws ReplacerException {
        // Base set-up
        super.setUp();

        // Initialize the finders
        baselineFinder = new BaselineFinder();
        centuryFinder = new CenturyFinder();
        centuryNewFinder = new CenturyNewFinder();
    }

    @Benchmark
    public void baselineFinder(Blackhole bh) {
        runFinder(baselineFinder, bh);
    }

    @Benchmark
    public void centuryFinder(Blackhole bh) {
        runReplacementFinder(centuryFinder, bh);
    }

    @Benchmark
    public void centuryNewFinder(Blackhole bh) {
        runReplacementFinder(centuryNewFinder, bh);
    }

    @SneakyThrows
    public static void main(String[] args) {
        run(CenturyFinderJmhBenchmark.class, fileName);

        generateChart(fileName);
    }
}
