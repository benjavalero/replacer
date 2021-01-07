package es.bvalero.replacer.finder.benchmark;

import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class RedirectMatcherBenchmark extends BaseFinderBenchmark {

    private static final int ITERATIONS = 1000;

    @Test
    void testRedirectMatcherBenchmark() throws Exception {
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

        MatcherAssert.assertThat(true, is(true));
    }
}
