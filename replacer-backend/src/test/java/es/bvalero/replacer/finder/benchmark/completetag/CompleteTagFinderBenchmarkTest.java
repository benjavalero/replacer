package es.bvalero.replacer.finder.benchmark.completetag;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.domain.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = XmlConfiguration.class)
class CompleteTagFinderBenchmarkTest extends BaseFinderBenchmark {

    @Resource
    private Set<String> completeTags;

    @Test
    void testBenchmark() throws ReplacerException {
        // Load the finders
        // In order to capture nested tags we can only use lazy regex
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new CompleteTagRegexIteratedFinder(completeTags));
        finders.add(new CompleteTagRegexBackReferenceFinder(completeTags));
        finders.add(new CompleteTagLinearIteratedFinder(completeTags));
        finders.add(new CompleteTagLinearFinder(completeTags));

        runBenchmark(finders);

        assertTrue(true);
    }
}
