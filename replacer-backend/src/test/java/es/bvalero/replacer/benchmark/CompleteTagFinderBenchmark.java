package es.bvalero.replacer.benchmark;

import es.bvalero.replacer.wikipedia.WikipediaConfig;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {WikipediaServiceImpl.class, WikipediaConfig.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class CompleteTagFinderBenchmark {

    private static final int ITERATIONS = 1000;

    @Autowired
    private WikipediaService wikipediaService;

    @Test
    public void testBenchmark() throws IOException, WikipediaException, URISyntaxException {
        // Load IDs of the sample articles
        List<Integer> sampleIds = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(CompleteTagFinderBenchmark.class.getResource("/es/bvalero/replacer/benchmark/sample-articles.txt").toURI()))) {
            stream.forEach(line -> sampleIds.add(Integer.valueOf(line.trim())));
        }

        // Load sample articles
        List<WikipediaPage> sampleContents = wikipediaService.getPagesByIds(sampleIds);

        List<String> words = Arrays.asList("math", "source", "syntaxhighlight", "blockquote", "pre", "score", "poem", "ref");

        // Load the finders
        List<CompleteTagAbstractFinder> finders = new ArrayList<>();
        finders.add(new CompleteTagRegexFinder(words));
        finders.add(new CompleteTagRegexLazyLazyFinder(words));
        finders.add(new CompleteTagRegexNotLazyFinder(words));
        finders.add(new CompleteTagRegexLazyNotFinder(words));
        finders.add(new CompleteTagRegexNegatedFinder(words));
        finders.add(new CompleteTagRegexLazyLazyNegatedFinder(words));
        finders.add(new CompleteTagRegexNotLazyNegatedFinder(words));
        finders.add(new CompleteTagRegexLazyNotNegatedFinder(words));
        finders.add(new CompleteTagAutomatonFinder(words));
        finders.add(new CompleteTagAutomatonNegatedFinder(words));

        // There are little differences between the regex ones
        // so we get the lazy one to test a regex with back-references
        finders.add(new CompleteTagRegexAllBackFinder(words));

        System.out.println();
        System.out.println("FINDER\tTIME");
        sampleContents.forEach(value -> {
            for (CompleteTagAbstractFinder finder : finders) {
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
