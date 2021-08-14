package es.bvalero.replacer.finder.immutable;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.FalsePositiveParser;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.MatcherAssert;
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
        LinkFinder.class,
        PersonNameFinder.class,
        PersonSurnameFinder.class,
        QuotesAngularFinder.class,
        QuotesDoubleFinder.class,
        QuotesTypographicFinder.class,
        CompleteTemplateFinder.class,
        SimpleMisspellingParser.class,
        SimpleMisspellingLoader.class,
        UppercaseAfterFinder.class,
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

        MatcherAssert.assertThat(true, is(true));
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
                        finder.findList(text);
                    }
                    double end = (double) (System.nanoTime() - start) / 1000.0; // In µs
                    if (print) {
                        System.out.println(finder.getClass().getSimpleName() + "\t" + end);
                    }
                }
            }
        );
    }

    @Test
    void testMatches() throws ReplacerException {
        // Load the finders
        List<ImmutableFinder> finders = new ArrayList<>();
        finders.add(new UrlFinder());
        finders.add(new XmlTagFinder());
        finders.add(new CompleteTagFinder());
        finders.add(new QuotesDoubleFinder());
        finders.add(new QuotesTypographicFinder());
        finders.add(new QuotesAngularFinder());

        List<String> sampleContents = findSampleContents();

        finders.forEach(
            finder -> {
                System.out.println("FINDER: " + finder.getClass().getSimpleName());
                sampleContents.forEach(
                    content -> {
                        finder.findList(content).forEach(result -> System.out.println("==> " + result.getText()));
                        System.out.println("----------");
                    }
                );
                System.out.println();
            }
        );

        MatcherAssert.assertThat(true, is(true));
    }
}
