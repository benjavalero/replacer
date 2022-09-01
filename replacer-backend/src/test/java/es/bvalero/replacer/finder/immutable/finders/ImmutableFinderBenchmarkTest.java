package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
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
        ImmutableFinder.class,
        XmlConfiguration.class,
        CommentFinder.class,
        CompleteTagFinder.class,
        CursiveFinder.class,
        ListingOfflineFinder.class,
        FalsePositiveParser.class,
        FalsePositiveLoader.class,
        FalsePositiveFinder.class,
        IgnorableSectionFinder.class,
        IgnorableTemplateFinder.class,
        LinkFinder.class,
        PersonNameFinder.class,
        PersonSurnameFinder.class,
        QuotesAngularFinder.class,
        QuotesDoubleFinder.class,
        QuotesTypographicFinder.class,
        TemplateFinder.class,
        TitleFinder.class,
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

    private static final String fileName = "../immutable/finders/immutable-benchmark.csv";

    @Autowired
    private List<ImmutableFinder> immutableFinders;

    @Autowired
    private SimpleMisspellingLoader simpleMisspellingLoader;

    @Autowired
    private FalsePositiveLoader falsePositiveLoader;

    @Test
    void testBenchmark() throws ReplacerException, IOException {
        // Load false positives
        falsePositiveLoader.load();

        // Load misspellings
        simpleMisspellingLoader.load();

        run(immutableFinders);

        assertTrue(true);
    }

    private void run(List<ImmutableFinder> finders) throws ReplacerException, IOException {
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
        List<ImmutableFinder> finders,
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
            for (ImmutableFinder finder : finders) {
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
        generateBoxplot(fileName, "Immutables");
    }
}
