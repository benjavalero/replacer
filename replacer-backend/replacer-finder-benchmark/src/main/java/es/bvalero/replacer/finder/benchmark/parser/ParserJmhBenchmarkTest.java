package es.bvalero.replacer.finder.benchmark.parser;

import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import es.bvalero.replacer.finder.parser.Parser;
import es.bvalero.replacer.finder.parser.ParserClassic;
import es.bvalero.replacer.finder.parser.Scanner;
import es.bvalero.replacer.wikipedia.WikipediaException;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

public class ParserJmhBenchmarkTest extends BaseFinderJmhBenchmark {

    private static final String fileName = "parser/parser-summary-jmh";

    @Setup
    public void setUp() throws WikipediaException {
        // Base set-up
        super.setUp();
    }

    @Benchmark
    public void scanner(Blackhole bh) {
        sampleContents.forEach(page -> new Scanner().scan(page.getContent()).forEach(bh::consume));
    }

    @Benchmark
    public void parser(Blackhole bh) {
        sampleContents.forEach(page -> new Parser().parse(page.getContent()).forEach(bh::consume));
    }

    @Benchmark
    public void parserClassic(Blackhole bh) {
        sampleContents.forEach(page -> new ParserClassic().parse(page.getContent()).forEach(bh::consume));
    }

    @Benchmark
    public void parserFind(Blackhole bh) {
        sampleContents.forEach(page -> new Parser().find(page.getContent()).forEach(bh::consume));
    }

    @Benchmark
    public void parserClassicFind(Blackhole bh) {
        sampleContents.forEach(page -> new ParserClassic().find(page.getContent()).forEach(bh::consume));
    }

    public static void main(String[] args) throws RunnerException {
        run(ParserJmhBenchmarkTest.class, fileName);
    }
}
