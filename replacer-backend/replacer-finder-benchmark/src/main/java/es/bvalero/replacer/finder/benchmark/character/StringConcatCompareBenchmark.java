package es.bvalero.replacer.finder.benchmark.character;

import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class StringConcatCompareBenchmark extends BaseFinderJmhBenchmark {

    private static final String fileName = "character/string-summary-jmh";

    private static final String str1 = "abc";
    private static final String str2 = "d";
    private static final String str3 = "";
    private static final String str4 = "efg hij áéí";

    @Override
    @Setup
    public void setUp() {
        // Base set-up
    }

    @Benchmark
    public void compareConcatPlus(Blackhole bh) {
        bh.consume(str1 + str2 + str3 + str4);
    }

    @Benchmark
    public void compareConcatFormatted(Blackhole bh) {
        // It is equivalent to "str".formatted()
        bh.consume(String.format("%s%s%s%s", str1, str2, str3, str4));
    }

    @Benchmark
    public void compareConcatBuilder(Blackhole bh) {
        bh.consume(new StringBuilder().append(str1).append(str2).append(str3).append(str4).toString());
    }

    @Benchmark
    public void compareConcatBuffer(Blackhole bh) {
        bh.consume(new StringBuffer().append(str1).append(str2).append(str3).append(str4).toString());
    }

    // TODO: String templates when they are ready

    public static void main(String[] args) throws RunnerException, ReplacerException {
        run(StringConcatCompareBenchmark.class, fileName);

        generateChart(fileName);
    }
}
