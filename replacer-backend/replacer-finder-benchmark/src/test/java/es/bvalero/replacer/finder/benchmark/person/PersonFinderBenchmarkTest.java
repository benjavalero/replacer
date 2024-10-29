package es.bvalero.replacer.finder.benchmark.person;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@EnableConfigurationProperties(FinderProperties.class)
@SpringBootTest(classes = BenchmarkFinder.class)
class PersonFinderBenchmarkTest extends BaseFinderBenchmark {

    private static final String fileName = "person/person-benchmark.csv";

    @Autowired
    private FinderProperties finderProperties;

    @Test
    void testBenchmark() throws ReplacerException {
        // Load the finders
        Set<String> personNames = new HashSet<>(this.finderProperties.getPersonNames());
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new PersonIndexOfFinder(personNames));
        // finders.add(new PersonRegexFinder(personNames)); // Medium
        // finders.add(new PersonAutomatonFinder(personNames)); // Long
        // finders.add(new PersonRegexCompleteFinder(personNames)); // Medium
        // finders.add(new PersonAutomatonCompleteFinder(personNames)); // Long
        // finders.add(new PersonRegexAlternateFinder(personNames)); // Very long
        finders.add(new PersonAutomatonAlternateFinder(personNames));
        // finders.add(new PersonRegexAlternateCompleteFinder(personNames)); // Very long
        finders.add(new PersonAutomatonAlternateCompleteFinder(personNames));
        finders.add(new PersonAhoCorasickFinder(personNames));

        // Use the Aho-Corasick algorithm which eventually creates an automaton
        // The whole-word finder cannot be used here as it doesn't work for expressions
        finders.add(new PersonAhoCorasickFinder(personNames));
        finders.add(new PersonAhoCorasickLongestFinder(personNames));
        finders.add(new PersonAhoCorasickWholeLongestFinder(personNames)); // About 2x faster than the best automaton
        // NOTE: These finders support a case-insensitive flag but the performance is reduced significantly

        List<Finder<?>> benchmarkFinders = new ArrayList<>(finders);
        runBenchmark(benchmarkFinders, fileName);

        assertTrue(true);
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        generateBoxplot(fileName, "Person");
    }
}
