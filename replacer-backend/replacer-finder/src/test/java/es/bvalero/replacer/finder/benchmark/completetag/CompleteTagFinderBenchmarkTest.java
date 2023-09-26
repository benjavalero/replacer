package es.bvalero.replacer.finder.benchmark.completetag;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@EnableConfigurationProperties(FinderProperties.class)
@SpringBootTest(classes = BenchmarkFinder.class)
class CompleteTagFinderBenchmarkTest extends BaseFinderBenchmark {

    private static final String fileName = "completetag/complete-tag-benchmark.csv";

    @Autowired
    private FinderProperties finderProperties;

    @Test
    void testBenchmark() throws ReplacerException {
        Set<String> completeTags = this.finderProperties.getCompleteTags();
        // Load the finders
        // In order to capture nested tags we can only use lazy regex
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new CompleteTagRegexIteratedFinder(completeTags));
        finders.add(new CompleteTagRegexBackReferenceFinder(completeTags));
        finders.add(new CompleteTagLinearIteratedFinder(completeTags));
        finders.add(new CompleteTagLinearFinder(completeTags));

        List<Finder<?>> benchmarkFinders = new ArrayList<>(finders);
        runBenchmark(benchmarkFinders, fileName);

        assertTrue(true);
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        generateBoxplot(fileName, "Complete Tag");
    }
}
