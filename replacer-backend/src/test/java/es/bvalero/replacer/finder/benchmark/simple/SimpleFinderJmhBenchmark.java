package es.bvalero.replacer.finder.benchmark.simple;

import es.bvalero.replacer.finder.benchmark.BaseFinderJmhBenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class SimpleFinderJmhBenchmark extends BaseFinderJmhBenchmark {

    private final String word = "_";
    private final SimpleLinearFinder simpleLinearFinder = new SimpleLinearFinder(word);
    private final SimpleRegexFinder simpleRegexFinder = new SimpleRegexFinder(word);
    private final SimpleAutomatonFinder simpleAutomatonFinder = new SimpleAutomatonFinder(word);

    @Benchmark
    public void testSimpleLinearFinder() {
        runFinder(simpleLinearFinder);
    }

    @Benchmark
    public void testSimpleRegexFinder() {
        runFinder(simpleRegexFinder);
    }

    @Benchmark
    public void testSimpleAutomatonFinder() {
        runFinder(simpleAutomatonFinder);
    }
}
