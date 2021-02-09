package es.bvalero.replacer.finder.replacement;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaUtils;
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
        ReplacementFinder.class,
        AcuteOFinder.class,
        DateFinder.class,
        MisspellingComposedManager.class,
        MisspellingComposedFinder.class,
        MisspellingManager.class,
        MisspellingSimpleFinder.class,
    }
)
class ReplacementFinderBenchmarkTest extends BaseFinderBenchmark {

    @Autowired
    private List<ReplacementFinder> replacementFinders;

    @Autowired
    private MisspellingManager misspellingManager;

    @Autowired
    private MisspellingComposedManager misspellingComposedManager;

    @MockBean
    private WikipediaService wikipediaService;

    @MockBean
    private ReplacementService replacementService;

    @Test
    void testBenchmark() throws ReplacerException {
        // Load composed misspellings
        String text = WikipediaUtils.getFileContent("/offline/composed-misspellings.txt");
        Set<Misspelling> composedMisspellings = misspellingComposedManager.parseItemsText(text);
        SetValuedMap<WikipediaLanguage, Misspelling> composedMisspellingMap = new HashSetValuedHashMap<>();
        composedMisspellingMap.putAll(WikipediaLanguage.getDefault(), composedMisspellings);
        misspellingComposedManager.setItems(composedMisspellingMap);

        // Load misspellings
        text = WikipediaUtils.getFileContent("/offline/misspelling-list.txt");
        Set<Misspelling> misspellings = misspellingManager.parseItemsText(text);
        SetValuedMap<WikipediaLanguage, Misspelling> misspellingMap = new HashSetValuedHashMap<>();
        misspellingMap.putAll(WikipediaLanguage.getDefault(), misspellings);
        misspellingManager.setItems(misspellingMap);

        run(replacementFinders);

        MatcherAssert.assertThat(true, is(true));
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
        sampleContents.forEach(
            text -> {
                for (ReplacementFinder finder : finders) {
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
}