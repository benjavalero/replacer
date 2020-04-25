package es.bvalero.replacer.finder.benchmark.completetag;

import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = XmlConfiguration.class)
public class CompleteTagFinderBenchmark extends BaseFinderBenchmark {
    private static final int ITERATIONS = 1000;

    @Resource
    private Set<String> completeTags;

    @Test
    public void testBenchmark() throws IOException, URISyntaxException {
        // Load the finders
        // In order to capture nested tags we can only use lazy regex
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new CompleteTagRegexIteratedFinder(completeTags));
        finders.add(new CompleteTagRegexBackReferenceFinder(completeTags));
        finders.add(new CompleteTagLinearIteratedFinder(completeTags));
        finders.add(new CompleteTagLinearFinder(completeTags));

        System.out.println();
        System.out.println("FINDER\tTIME");
        findSampleContents()
            .forEach(
                value -> {
                    for (BenchmarkFinder finder : finders) {
                        long start = System.currentTimeMillis();
                        for (int i = 0; i < ITERATIONS; i++) {
                            finder.findMatches(value);
                        }
                        long end = System.currentTimeMillis() - start;
                        System.out.println(finder.getClass().getSimpleName() + "\t" + end);
                    }
                }
            );

        Assertions.assertTrue(true);
    }
}
