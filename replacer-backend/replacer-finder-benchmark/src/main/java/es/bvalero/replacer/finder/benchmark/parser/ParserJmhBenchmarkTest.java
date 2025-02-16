package es.bvalero.replacer.finder.benchmark.parser;

import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.parser.Parser;
import es.bvalero.replacer.finder.parser.Scanner;
import es.bvalero.replacer.wikipedia.WikipediaException;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

@Warmup(time = 5)
@Measurement(time = 5)
public class ParserJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "parser/parser-summary-jmh";

    @Setup
    public void setUp() throws WikipediaException {
        // Base set-up
        super.setUp();
    }

    @Benchmark
    public void scan(Blackhole bh) {
        sampleContents.forEach(page -> new Scanner().scan(page.getContent()).forEach(bh::consume));
    }

    @Benchmark
    public void expressionTree(Blackhole bh) {
        sampleContents.forEach(page -> new Parser().expressionTree(page.getContent()).forEach(bh::consume));
    }

    @Benchmark
    public void expressionIterable(Blackhole bh) {
        sampleContents.forEach(page -> new Parser().expressionIterable(page.getContent()).forEach(bh::consume));
    }

    public static void main(String[] args) throws RunnerException {
        run(ParserJmhBenchmarkTest.class, fileName);
    }
}
