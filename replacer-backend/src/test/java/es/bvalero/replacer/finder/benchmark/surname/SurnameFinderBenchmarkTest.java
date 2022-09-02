package es.bvalero.replacer.finder.benchmark.surname;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.config.XmlConfiguration;
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
class SurnameFinderBenchmarkTest extends BaseFinderBenchmark {

    private static final String fileName = "surname/surname-benchmark.csv";

    @Resource
    private Set<String> personSurnames;

    @Test
    void testBenchmark() throws ReplacerException {
        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new SurnameIndexOfFinder(personSurnames));
        // finders.add(new SurnameRegexFinder(personSurnames)); // Medium
        // finders.add(new SurnameAutomatonFinder(personSurnames)); // Long
        // finders.add(new SurnameRegexCompleteFinder(personSurnames)); // Very long
        // finders.add(new SurnameAutomatonCompleteFinder(personSurnames)); // Long
        // finders.add(new SurnameRegexAlternateFinder(personSurnames)); // Very long
        finders.add(new SurnameAutomatonAlternateFinder(personSurnames));
        // finders.add(new SurnameRegexAlternateCompleteFinder(personSurnames)); // Short
        finders.add(new SurnameAutomatonAlternateCompleteFinder(personSurnames));

        List<Finder<?>> benchmarkFinders = new ArrayList<>(finders);
        runBenchmark(benchmarkFinders, fileName);

        assertTrue(true);
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        generateBoxplot(fileName, "Surname");
    }
}
