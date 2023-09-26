package es.bvalero.replacer.finder.benchmark.redirection;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@EnableConfigurationProperties(FinderProperties.class)
@SpringBootTest(classes = BenchmarkFinder.class)
class RedirectionFinderBenchmarkTest extends BaseFinderBenchmark {

    private static final String fileName = "redirection/redirection-benchmark.csv";

    @Autowired
    private FinderProperties finderProperties;

    @Test
    void testBenchmark() throws ReplacerException {
        Set<String> ignorableTemplates =
            this.finderProperties.getIgnorableTemplates()
                .stream()
                .filter(s -> s.contains("#"))
                .map(FinderUtils::toLowerCase)
                .collect(Collectors.toUnmodifiableSet());
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
