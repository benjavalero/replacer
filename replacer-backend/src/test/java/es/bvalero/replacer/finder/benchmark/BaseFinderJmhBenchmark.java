package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.api.WikipediaUtils;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

@Fork(1)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 3, time = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class BaseFinderJmhBenchmark {

    private List<WikipediaPage> sampleContents;

    @Setup
    public void setUp() throws WikipediaException, ReplacerException {
        sampleContents = WikipediaUtils.findSampleContents();
    }

    public void runFinder(Finder<?> finder) {
        sampleContents.forEach(page -> finder.find(page).forEach(result -> {}));
    }
}
