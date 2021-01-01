package es.bvalero.replacer.finder.benchmark;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.immutables.*;
import es.bvalero.replacer.finder.misspelling.*;
import es.bvalero.replacer.replacement.ReplacementDao;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaServiceOfflineImpl;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(
    classes = {
        ImmutableFinder.class,
        XmlConfiguration.class,
        CategoryFinder.class,
        CommentFinder.class,
        CompleteTagFinder.class,
        CursiveFinder.class,
        FalsePositiveManager.class,
        FalsePositiveFinder.class,
        FileNameFinder.class,
        InterLanguageLinkFinder.class,
        LinkAliasedFinder.class,
        LinkSuffixedFinder.class,
        PersonNameFinder.class,
        PersonSurnameFinder.class,
        QuotesAngularFinder.class,
        QuotesFinder.class,
        QuotesTypographicFinder.class,
        CompleteTemplateFinder.class,
        MisspellingManager.class,
        UppercaseAfterFinder.class,
        UrlFinder.class,
        XmlTagFinder.class,
    }
)
class ImmutableFinderBenchmark extends BaseFinderBenchmark {

    @Autowired
    private List<ImmutableFinder> immutableFinders;

    @Autowired
    private MisspellingManager misspellingManager;

    @Autowired
    private FalsePositiveManager falsePositiveManager;

    @MockBean
    private WikipediaService wikipediaService;

    @MockBean
    private ReplacementDao replacementDao;

    @Test
    void testBenchmark() throws Exception {
        WikipediaService wikipediaServiceOffline = new WikipediaServiceOfflineImpl();

        // Load false positives
        String text = wikipediaServiceOffline.getFalsePositiveListPageContent(WikipediaLanguage.getDefault());
        Set<String> falsePositives = falsePositiveManager.parseItemsText(text);
        SetValuedMap<WikipediaLanguage, String> falsePositiveMap = new HashSetValuedHashMap<>();
        falsePositiveMap.putAll(WikipediaLanguage.getDefault(), falsePositives);
        falsePositiveManager.setItems(falsePositiveMap);

        // Load misspellings
        text = wikipediaServiceOffline.getMisspellingListPageContent(WikipediaLanguage.getDefault());
        Set<Misspelling> misspellings = misspellingManager.parseItemsText(text);
        SetValuedMap<WikipediaLanguage, Misspelling> misspellingMap = new HashSetValuedHashMap<>();
        misspellingMap.putAll(WikipediaLanguage.getDefault(), misspellings);
        misspellingManager.setItems(misspellingMap);

        run(immutableFinders);

        MatcherAssert.assertThat(true, is(true));
    }

    private void run(List<ImmutableFinder> finders) throws IOException, URISyntaxException {
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
                        IterableUtils.toList(finder.findList(text));
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
    void testMatches() throws Exception {
        // Load the finders
        List<ImmutableFinder> finders = new ArrayList<>();
        finders.add(new UrlFinder());
        finders.add(new XmlTagFinder());
        finders.add(new CompleteTagFinder());
        finders.add(new QuotesFinder());
        finders.add(new QuotesTypographicFinder());
        finders.add(new QuotesAngularFinder());
        finders.add(new FileNameFinder());
        finders.add(new LinkSuffixedFinder());
        finders.add(new LinkAliasedFinder());
        finders.add(new InterLanguageLinkFinder());

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
