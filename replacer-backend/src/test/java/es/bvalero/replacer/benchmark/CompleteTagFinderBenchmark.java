package es.bvalero.replacer.benchmark;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompleteTagFinderBenchmark extends BaseFinderBenchmark {

    private static final int ITERATIONS = 1000;

    @Test
    public void testBenchmark() throws IOException, URISyntaxException {
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
        findSampleContents().forEach(value -> {
            for (CompleteTagAbstractFinder finder : finders) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < ITERATIONS; i++) {
                    finder.findMatches(value);
                }
                long end = System.currentTimeMillis() - start;
                System.out.println(finder.getClass().getSimpleName() + "\t" + end);
            }
        });

        Assert.assertTrue(true);
    }

}
