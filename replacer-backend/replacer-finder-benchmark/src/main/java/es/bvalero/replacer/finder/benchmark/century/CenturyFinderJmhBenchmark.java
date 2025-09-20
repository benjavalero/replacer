package es.bvalero.replacer.finder.benchmark.century;

import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
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

    @Override
    protected void runFinder(Finder<?> finder, Blackhole bh) {
        assert finder instanceof ReplacementFinder;
        ReplacementFinder replacementFinder = (ReplacementFinder) finder;
        // As the sample represents the whole dump,
        // finding all over the sample represents the time finding all over the dump.
        // NOTE: therefore, the average time corresponds to run the finder in the 50 sample pages.
        sampleContents.forEach(page -> replacementFinder.findWithNoSuggestions(page).forEach(bh::consume));
    }

    @SneakyThrows
    public static void main(String[] args) {
        run(CenturyFinderJmhBenchmark.class, fileName);

        generateChart(fileName);
    }
}
