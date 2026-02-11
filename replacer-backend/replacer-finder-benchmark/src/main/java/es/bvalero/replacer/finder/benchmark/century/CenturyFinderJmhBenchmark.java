package es.bvalero.replacer.finder.benchmark.century;

import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.replacement.finders.CenturyFinder;
import lombok.SneakyThrows;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Warmup(time = 5) // Base: 5 iterations, 2 s each
@Measurement(time = 5) // Base: 5 iterations, 2 s each
public class CenturyFinderJmhBenchmark extends BaseFinderJmhBenchmark {

    private static final String fileName = "century/century-summary-jmh";

    private CenturyFinder centuryFinder;
    private CenturyNewFinder centuryNewFinder;

    @Override
    @Setup
    public void setUp() throws ReplacerException {
        // Base set-up
        super.setUp();

        // Initialize the finders
        centuryFinder = new CenturyFinder();
        centuryNewFinder = new CenturyNewFinder();
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
