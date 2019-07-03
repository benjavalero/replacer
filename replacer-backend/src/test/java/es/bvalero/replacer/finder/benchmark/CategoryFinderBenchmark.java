package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.authentication.AuthenticationServiceImpl;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {WikipediaServiceImpl.class, AuthenticationServiceImpl.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class CategoryFinderBenchmark {

    private final static int ITERATIONS = 1000;

    @Autowired
    private WikipediaService wikipediaService;

    @Test
    public void testBenchmark() throws IOException, WikipediaException, URISyntaxException {
        // Load IDs of the sample articles
        List<Integer> sampleIds = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(CategoryFinderBenchmark.class.getResource("/benchmark/sample-articles.txt").toURI()))) {
            stream.forEach(line -> sampleIds.add(Integer.valueOf(line.trim())));
        }

        // Load sample articles
        List<WikipediaPage> sampleContents = wikipediaService.getPagesByIds(sampleIds);

        // Load the finders
        List<CategoryAbstractFinder> finders = new ArrayList<>();
        finders.add(new CategoryRegexFinder());
        finders.add(new CategoryRegexClassFinder());
        finders.add(new CategoryRegexClassLazyFinder());
        finders.add(new CategoryRegexClassPossessiveFinder());
        finders.add(new CategoryAutomatonFinder());

        System.out.println();
        System.out.println("FINDER\tTIME");
        sampleContents.forEach(value -> {
            for (CategoryAbstractFinder finder : finders) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < ITERATIONS; i++) {
                    finder.findMatches(value.getContent());
                }
                long end = System.currentTimeMillis() - start;
                System.out.println(finder.getClass().getSimpleName() + "\t" + end);
            }
        });

        Assert.assertTrue(true);
    }

}
