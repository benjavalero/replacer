package es.bvalero.replacer.finder.benchmark.person;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = XmlConfiguration.class)
class PersonFinderBenchmark extends BaseFinderBenchmark {
    @Resource
    private Set<String> personNames;

    @Test
    void testBenchmark() throws Exception {
        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new PersonIndexOfFinder(personNames));
        finders.add(new PersonRegexFinder(personNames));
        finders.add(new PersonAutomatonFinder(personNames));
        finders.add(new PersonRegexCompleteFinder(personNames));
        finders.add(new PersonAutomatonCompleteFinder(personNames));
        finders.add(new PersonRegexAlternateFinder(personNames));
        finders.add(new PersonAutomatonAlternateFinder(personNames));
        finders.add(new PersonRegexAlternateCompleteFinder(personNames));
        finders.add(new PersonAutomatonAlternateCompleteFinder(personNames));
        finders.add(new PersonRegexAllFinder(personNames));
        finders.add(new PersonAutomatonAllFinder(personNames));
        finders.add(new PersonRegexAllCompleteFinder(personNames));

        runBenchmark(finders);

        MatcherAssert.assertThat(true, is(true));
    }
}
