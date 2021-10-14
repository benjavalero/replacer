package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import java.util.List;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    classes = {
        ImmutableFinder.class,
        XmlConfiguration.class,
        CommentFinder.class,
        CompleteTagFinder.class,
        CursiveFinder.class,
        ListingOfflineFinder.class,
        FalsePositiveParser.class,
        FalsePositiveLoader.class,
        FalsePositiveFinder.class,
        IgnorableTemplateFinder.class,
        LinkFinder.class,
        PersonNameFinder.class,
        PersonSurnameFinder.class,
        QuotesAngularFinder.class,
        QuotesDoubleFinder.class,
        QuotesTypographicFinder.class,
        TemplateFinder.class,
        SimpleMisspellingParser.class,
        SimpleMisspellingLoader.class,
        TableFinder.class,
        UppercaseFinder.class,
        UrlFinder.class,
        XmlTagFinder.class,
    }
)
@ActiveProfiles("offline")
class ImmutableFinderBenchmarkTest extends BaseFinderBenchmark {

    @Autowired
    private List<ImmutableFinder> immutableFinders;

    @Autowired
    private SimpleMisspellingLoader simpleMisspellingLoader;

    @Autowired
    private FalsePositiveLoader falsePositiveLoader;

    @Test
    void testBenchmark() throws ReplacerException {
        // Load false positives
        falsePositiveLoader.load();

        // Load misspellings
        simpleMisspellingLoader.load();

        run(immutableFinders);

        assertTrue(true);
    }

    private void run(List<ImmutableFinder> finders) throws ReplacerException {
        List<String> sampleContents = findSampleContents();

        // Warm-up
        System.out.println("WARM-UP...");
        run(finders, WARM_UP, sampleContents, false);

        // Real run
        run(finders, ITERATIONS, sampleContents, true);
    }

    private void run(List<ImmutableFinder> finders, int numIterations, List<String> sampleContents, boolean print) {
        if (print) {
            System.out.println();
            System.out.println("FINDER\tTIME");
        }
        sampleContents.forEach(
            text -> {
                for (ImmutableFinder finder : finders) {
                    long start = System.nanoTime();
                    for (int i = 0; i < numIterations; i++) {
                        // Only transform the iterable without validating not to penalize the performance of the benchmark
                        IterableUtils.toList(finder.find(FinderPage.of(text)));
                    }
                    double end = (double) (System.nanoTime() - start) / 1000.0; // In µs
                    if (print) {
                        System.out.println(finder.getClass().getSimpleName() + "\t" + end);
                    }
                }
            }
        );
    }
}
