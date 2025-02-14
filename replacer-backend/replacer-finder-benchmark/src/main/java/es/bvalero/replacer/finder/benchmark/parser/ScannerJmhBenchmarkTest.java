package es.bvalero.replacer.finder.benchmark.parser;

import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.parser.Scanner;
import es.bvalero.replacer.wikipedia.WikipediaException;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

public class ScannerJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "parser/scanner-summary-jmh";

    @Setup
    public void setUp() throws WikipediaException {
        // Base set-up
        super.setUp();
    }

    @Benchmark
    public void scanner(Blackhole bh) {
        sampleContents.forEach(page -> new Scanner().scan(page.getContent()).forEach(bh::consume));
    }

    public static void main(String[] args) throws RunnerException {
        run(ScannerJmhBenchmarkTest.class, fileName);
    }
}
