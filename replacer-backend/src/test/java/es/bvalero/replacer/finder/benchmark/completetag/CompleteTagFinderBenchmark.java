package es.bvalero.replacer.finder.benchmark.completetag;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import org.junit.Assert;
import org.junit.Test;

public class CompleteTagFinderBenchmark extends BaseFinderBenchmark {
    private static final int ITERATIONS = 1000;

    @Test
    public void testBenchmark() throws IOException, URISyntaxException {
        List<String> words = Arrays.asList(
            "blockquote",
            "cite",
            "code",
            "math",
            "nowiki",
            "poem",
            "pre",
            "ref",
            "score",
            "source",
            "syntaxhighlight"
        );

        // Load the finders
        // In order to capture nested tags we can only use lazy regex
        List<CompleteTagFinder> finders = new ArrayList<>();
        finders.add(new CompleteTagRegexLazyFinder(words));
        finders.add(new CompleteTagRegexNegatedLazyFinder(words));
        finders.add(new CompleteTagRegexAlternateFinder(words));
        finders.add(new CompleteTagRegexAlternateNegatedFinder(words));

        System.out.println();
        System.out.println("FINDER\tTIME");
        findSampleContents()
            .forEach(
                value -> {
                    for (CompleteTagFinder finder : finders) {
                        long start = System.currentTimeMillis();
                        for (int i = 0; i < ITERATIONS; i++) {
                            finder.findMatches(value);
                        }
                        long end = System.currentTimeMillis() - start;
                        System.out.println(finder.getClass().getSimpleName() + "\t" + end);
                    }
                }
            );

        Assert.assertTrue(true);
    }
}
