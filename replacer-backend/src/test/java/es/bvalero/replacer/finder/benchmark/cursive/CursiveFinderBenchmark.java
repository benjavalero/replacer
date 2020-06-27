package es.bvalero.replacer.finder.benchmark.cursive;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class CursiveFinderBenchmark extends BaseFinderBenchmark {

    @Test
    void testBenchmark() throws Exception {
        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new CursiveRegexDotLazyFinder());
        finders.add(new CursiveRegexFinder());
        finders.add(new CursiveRegexDotAllLookFinder());
        finders.add(new CursiveRegexLookFinder());
        finders.add(new CursiveAutomatonFinder());
        finders.add(new CursiveLinearFinder());

        runBenchmark(finders);

        MatcherAssert.assertThat(true, is(true));
    }
}
