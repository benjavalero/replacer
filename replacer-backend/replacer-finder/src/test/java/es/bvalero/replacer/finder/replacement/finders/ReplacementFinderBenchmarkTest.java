package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    classes = {
        ReplacementFinder.class,
        XmlConfiguration.class,
        AcuteOFinder.class,
        CenturyFinder.class,
        CoordinatesFinder.class,
        DateFinder.class,
        DegreeFinder.class,
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
    void testBenchmark() throws ReplacerException {
        // Load composed misspellings
        composedMisspellingLoader.load();

        // Load misspellings
        simpleMisspellingLoader.load();

        List<Finder<?>> finders = new ArrayList<>(replacementFinders);
        runBenchmark(finders, fileName);

        assertTrue(true);
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        generateBoxplot(fileName, "Replacements");
    }
}
