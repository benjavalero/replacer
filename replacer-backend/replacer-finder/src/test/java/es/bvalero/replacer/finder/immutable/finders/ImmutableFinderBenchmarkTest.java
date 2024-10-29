package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@EnableConfigurationProperties(FinderProperties.class)
@SpringBootTest(
    classes = {
        ImmutableFinder.class,
        CommentFinder.class,
        CompleteTagFinder.class,
        CursiveFinder.class,
        ListingOfflineFinder.class,
        FalsePositiveParser.class,
        FalsePositiveLoader.class,
        FalsePositiveFinder.class,
        IgnorableSectionFinder.class,
        RedirectionFinder.class,
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
        ComposedMisspellingParser.class,
        ComposedMisspellingLoader.class,
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
    private ComposedMisspellingLoader composedMisspellingLoader;

    @Autowired
    private FalsePositiveLoader falsePositiveLoader;

    @Test
    void testBenchmark() throws ReplacerException {
        // Load false positives
        falsePositiveLoader.load();

        // Load misspellings
        simpleMisspellingLoader.load();
        composedMisspellingLoader.load();

        List<Finder<?>> finders = new ArrayList<>(immutableFinders);
        runBenchmark(finders, fileName);

        assertTrue(true);
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        generateBoxplot(fileName, "Immutables");
    }
}
