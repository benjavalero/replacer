package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import java.util.List;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    classes = {
        ReplacementFinder.class,
        XmlConfiguration.class,
        AcuteOFinder.class,
        DateFinder.class,
        ListingOfflineFinder.class,
        ComposedMisspellingParser.class,
        ComposedMisspellingLoader.class,
        MisspellingComposedFinder.class,
        SimpleMisspellingParser.class,
        SimpleMisspellingLoader.class,
        MisspellingSimpleFinder.class,
    }
)
@ActiveProfiles("offline")
class ReplacementFinderBenchmarkTest extends BaseFinderBenchmark {

    @Autowired
    private List<ReplacementFinder> replacementFinders;

    @Autowired
    private SimpleMisspellingLoader simpleMisspellingLoader;

    @Autowired
    private ComposedMisspellingLoader composedMisspellingLoader;

    @Test
    void testBenchmark() throws ReplacerException {
        // Load composed misspellings
        composedMisspellingLoader.load();

        // Load misspellings
        simpleMisspellingLoader.load();

        run(replacementFinders);

        assertTrue(true);
    }

    private void run(List<ReplacementFinder> finders) throws ReplacerException {
        List<String> sampleContents = findSampleContents();

        // Warm-up
        System.out.println("WARM-UP...");
        run(finders, WARM_UP, sampleContents, false);

        // Real run
        run(finders, ITERATIONS, sampleContents, true);
    }

    private void run(List<ReplacementFinder> finders, int numIterations, List<String> sampleContents, boolean print) {
        if (print) {
            System.out.println();
            System.out.println("FINDER\tTIME");
        }
        sampleContents.forEach(text -> {
            for (ReplacementFinder finder : finders) {
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
        });
    }
}
