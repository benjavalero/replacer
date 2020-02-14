package es.bvalero.replacer.finder.benchmark;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class RedirectMatcherBenchmark extends BaseFinderBenchmark {
    private static final int ITERATIONS = 1000;

    @Test
    public void testRedirectMatcherBenchmark() throws IOException, URISyntaxException {
        // Load the matchers
        List<RedirectAbstractMatcher> matchers = new ArrayList<>();
        matchers.add(new RedirectLowercaseContainsMatcher()); // WINNER
        matchers.add(new RedirectContainsIgnoreCaseMatcher());
        matchers.add(new RedirectRegexInsensitiveMatcher());

        System.out.println();
        System.out.println("FINDER\tTIME");
        findSampleContents()
            .forEach(
                value -> {
                    for (RedirectAbstractMatcher matcher : matchers) {
                        long start = System.currentTimeMillis();
                        for (int i = 0; i < ITERATIONS; i++) {
                            matcher.isRedirect(value);
                        }
                        long end = System.currentTimeMillis() - start;
                        System.out.println(matcher.getClass().getSimpleName() + "\t" + end);
                    }
                }
            );

        Assert.assertTrue(true);
    }
}
