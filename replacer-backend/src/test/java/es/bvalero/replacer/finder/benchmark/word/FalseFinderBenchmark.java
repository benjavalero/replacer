package es.bvalero.replacer.finder.benchmark.word;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.misspelling.FalsePositiveFinder;
import es.bvalero.replacer.finder.misspelling.FalsePositiveManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(
    classes = { FalsePositiveFinder.class, FalsePositiveManager.class },
    initializers = ConfigFileApplicationContextInitializer.class
)
public class FalseFinderBenchmark extends BaseFinderBenchmark {
    private static final int ITERATIONS = 1000;

    @Autowired
    private FalsePositiveManager falsePositiveManager;

    @Autowired
    private FalsePositiveFinder falsePositiveFinder;

    /* NOTE: We can use the same finders that we use for misspellings just with a different set of words */

    @Test
    public void testWordFinderBenchmark() throws IOException, URISyntaxException {
        // Load the misspellings
        falsePositiveManager.update();
        Collection<String> words = falsePositiveFinder.getFalsePositives();

        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new WordIndexOfFinder(words));
        finders.add(new WordRegexFinder(words));
        finders.add(new WordAutomatonFinder(words)); // Discarded: we need to increase too much the heap size
        finders.add(new WordRegexCompleteFinder(words));
        finders.add(new WordRegexAlternateFinder(words));
        finders.add(new WordAutomatonAlternateFinder(words)); // Discarded: we need to increase too much the stack size
        finders.add(new WordRegexAlternateCompleteFinder(words));
        finders.add(new WordRegexAllFinder(words));
        finders.add(new WordAutomatonAllFinder(words));
        finders.add(new WordLinearAllFinder(words));
        finders.add(new WordRegexAllCompleteFinder(words));

        runBenchmark(finders);

        Assert.assertThat(true, is(true));
    }
}
