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
import java.util.List;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {WikipediaServiceImpl.class, WikipediaConfig.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class RedirectMatcherBenchmark {

    private static final int ITERATIONS = 1000;

    @Autowired
    private WikipediaService wikipediaService;

    @Test
    public void testRedirectMatcherBenchmark() throws IOException, WikipediaException, URISyntaxException {
        // Load IDs of the sample articles
        List<Integer> sampleIds = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(RedirectMatcherBenchmark.class.getResource("/es/bvalero/replacer/benchmark/sample-articles.txt").toURI()))) {
            stream.forEach(line -> sampleIds.add(Integer.valueOf(line.trim())));
        }

        // Load sample articles
        List<WikipediaPage> sampleContents = wikipediaService.getPagesByIds(sampleIds);

        // Load the matchers
        List<RedirectAbstractMatcher> matchers = new ArrayList<>();
        matchers.add(new RedirectLowercaseContainsMatcher()); // WINNER
        matchers.add(new RedirectContainsIgnoreCaseMatcher());
        matchers.add(new RedirectRegexInsensitiveMatcher());

        System.out.println();
        System.out.println("FINDER\tTIME");
        sampleContents.forEach(value -> {
            for (RedirectAbstractMatcher matcher : matchers) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < ITERATIONS; i++) {
                    matcher.isRedirect(value.getContent());
                }
                long end = System.currentTimeMillis() - start;
                System.out.println(matcher.getClass().getSimpleName() + "\t" + end);
            }
        });

        Assert.assertTrue(true);
    }

}
