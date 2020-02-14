package es.bvalero.replacer.finder.benchmark;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TemplateFinderBenchmark extends BaseFinderBenchmark {
    private static final int ITERATIONS = 1000;

    @Test
    public void testBenchmark() throws IOException, URISyntaxException {
        List<String> words = Arrays.asList(
            "ORDENAR",
            "DEFAULTSORT",
            "NF",
            "commonscat",
            "coord",
            "cita libro",
            "cita",
            "quote",
            "cquote",
            "caja de cita"
        );

        // Load the finders
        List<TemplateAbstractFinder> finders = new ArrayList<>();
        finders.add(new TemplateRegexFinder(words));
        finders.add(new TemplateRegexClassFinder(words));
        finders.add(new TemplateRegexAllFinder(words));
        finders.add(new TemplateRegexClassAllFinder(words));
        finders.add(new TemplateAutomatonFinder(words));
        finders.add(new TemplateAutomatonClassFinder(words));
        finders.add(new TemplateAutomatonAllFinder(words));
        finders.add(new TemplateAutomatonClassAllFinder(words)); // WINNER

        System.out.println();
        System.out.println("FINDER\tTIME");
        findSampleContents()
            .forEach(
                value -> {
                    for (TemplateAbstractFinder finder : finders) {
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
