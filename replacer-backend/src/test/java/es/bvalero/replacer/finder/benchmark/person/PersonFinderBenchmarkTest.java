package es.bvalero.replacer.finder.benchmark.person;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = XmlConfiguration.class)
class PersonFinderBenchmarkTest extends BaseFinderBenchmark {

    @Resource
    private Set<String> personNames;

    @Test
    void testBenchmark() throws ReplacerException {
        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new PersonIndexOfFinder(personNames));
        // finders.add(new PersonRegexFinder(personNames)); // Long
        // finders.add(new PersonAutomatonFinder(personNames)); // Long
        // finders.add(new PersonRegexCompleteFinder(personNames)); // Long
        // finders.add(new PersonAutomatonCompleteFinder(personNames)); // Long
        // finders.add(new PersonRegexAlternateFinder(personNames)); // Very long
        finders.add(new PersonAutomatonAlternateFinder(personNames));
        // finders.add(new PersonRegexAlternateCompleteFinder(personNames)); // Very long
        finders.add(new PersonAutomatonAlternateCompleteFinder(personNames));

        runBenchmark(finders, WARM_UP, ITERATIONS * 10);

        MatcherAssert.assertThat(true, is(true));
    }
}
