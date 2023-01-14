package es.bvalero.replacer.finder.benchmark.redirection;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = XmlConfiguration.class)
class RedirectionFinderBenchmarkTest extends BaseFinderBenchmark {

    private static final String fileName = "redirection/redirection-benchmark.csv";

    @Resource
    private Set<String> ignorableTemplates;

    @Test
    void testBenchmark() throws ReplacerException {
        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new RedirectionLowercaseContainsFinder(ignorableTemplates));
        finders.add(new RedirectionRegexFinder(ignorableTemplates));
        finders.add(new RedirectionRegexInsensitiveFinder(ignorableTemplates));
        finders.add(new RedirectionAutomatonFinder(ignorableTemplates));

        List<Finder<?>> benchmarkFinders = new ArrayList<>(finders);
        runBenchmark(benchmarkFinders, fileName);

        assertTrue(true);
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        generateBoxplot(fileName, "Redirection");
    }
}
