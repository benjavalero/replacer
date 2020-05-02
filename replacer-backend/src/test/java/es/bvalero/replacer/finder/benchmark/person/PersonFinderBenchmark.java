package es.bvalero.replacer.finder.benchmark.person;

import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PersonFinderBenchmark extends BaseFinderBenchmark {

    @Test
    public void testBenchmark() throws IOException, URISyntaxException {
        List<String> words = Arrays.asList("Domingo", "Frances", "Julio", "Los Angeles", "Manchester", "Sidney", "Sky");

        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new PersonIndexOfFinder(words));
        finders.add(new PersonRegexFinder(words));
        finders.add(new PersonAutomatonFinder(words));
        finders.add(new PersonRegexCompleteFinder(words));
        finders.add(new PersonAutomatonCompleteFinder(words));
        finders.add(new PersonRegexAlternateFinder(words));
        finders.add(new PersonAutomatonAlternateFinder(words));
        finders.add(new PersonRegexAlternateCompleteFinder(words));
        finders.add(new PersonAutomatonAlternateCompleteFinder(words));
        finders.add(new PersonRegexAllFinder(words));
        finders.add(new PersonAutomatonAllFinder(words));
        finders.add(new PersonRegexAllCompleteFinder(words));

        runBenchmark(finders);

        Assertions.assertTrue(true);
    }
}
