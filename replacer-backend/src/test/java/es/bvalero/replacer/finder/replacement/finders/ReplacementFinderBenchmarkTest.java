package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    classes = {
        ReplacementFinder.class,
        XmlConfiguration.class,
        AcuteOFinder.class,
        CenturyFinder.class,
        CoordinatesFinder.class,
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

    private static final String fileName = "../replacement/finders/replacement-benchmark.csv";

    @Autowired
    private List<ReplacementFinder> replacementFinders;

    @Autowired
    private SimpleMisspellingLoader simpleMisspellingLoader;

    @Autowired
    private ComposedMisspellingLoader composedMisspellingLoader;

    @Test
    void testBenchmark() throws ReplacerException, IOException {
        // Load composed misspellings
        composedMisspellingLoader.load();

        // Load misspellings
        simpleMisspellingLoader.load();

        run(replacementFinders);

        assertTrue(true);
    }

    private void run(List<ReplacementFinder> finders) throws ReplacerException, IOException {
        List<WikipediaPage> sampleContents = findSampleContents();

        // Warm-up
        run(finders, WARM_UP, sampleContents, null);

        // Real run
        String testResourcesPath = "src/test/resources/es/bvalero/replacer/finder/benchmark/";
        File csvFile = new File(testResourcesPath + fileName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
        run(finders, ITERATIONS, sampleContents, writer);
        writer.close();
    }

    private void run(
        List<ReplacementFinder> finders,
        int numIterations,
        List<WikipediaPage> sampleContents,
        @Nullable BufferedWriter writer
    ) {
        boolean print = (writer != null);
        if (print) {
            String headers = "FINDER\tTIME\n";
            print(writer, headers);
            System.out.print(headers);
        }
        sampleContents.forEach(page -> {
            for (ReplacementFinder finder : finders) {
                long start = System.nanoTime();
                for (int i = 0; i < numIterations; i++) {
                    // Only transform the iterable without validating not to penalize the performance of the benchmark
                    IterableUtils.toList(finder.find(page));
                }
                double end = (double) (System.nanoTime() - start) / 1000.0; // In µs
                if (print) {
                    String time = finder.getClass().getSimpleName() + '\t' + end + '\n';
                    print(writer, time);
                    System.out.print(time);
                }
            }
        });
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        generateBoxplot(fileName, "Replacements");
    }
}
