package es.bvalero.replacer.finder.benchmark.century;

import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
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

    private CenturyOldFinder centuryOldFinder;
    private CenturyNewFinder centuryNewFinder;

    @Override
    @Setup
    public void setUp() throws ReplacerException {
        // Base set-up
        super.setUp();

        // Initialize the finders
        centuryOldFinder = new CenturyOldFinder();
        centuryNewFinder = new CenturyNewFinder();
    }

    @Benchmark
    public void centuryOldFinder(Blackhole bh) {
        runFinder(centuryOldFinder, bh);
    }

    @Benchmark
    public void centuryNewFinder(Blackhole bh) {
        runFinder(centuryNewFinder, bh);
    }

    @SneakyThrows
    public static void main(String[] args) {
        run(CenturyFinderJmhBenchmark.class, fileName);

        generateChart(fileName);
    }
}
