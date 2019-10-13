package es.bvalero.replacer.benchmark;

import es.bvalero.replacer.wikipedia.WikipediaConfig;
import es.bvalero.replacer.misspelling.MisspellingFinder;
import es.bvalero.replacer.misspelling.MisspellingManager;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {MisspellingFinder.class, MisspellingManager.class, WikipediaServiceImpl.class, WikipediaConfig.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class WordFinderBenchmark {

    private static final int ITERATIONS = 1000;

    @Autowired
    private MisspellingManager misspellingManager;

    @Autowired
    private MisspellingFinder misspellingFinder;

    @Autowired
    private WikipediaService wikipediaService;

    @Test
    public void testWordFinderBenchmark() throws IOException, WikipediaException, URISyntaxException {
        // Load the misspellings
        misspellingManager.update();
        Collection<String> words = misspellingFinder.getMisspellingMap().keySet();

        // Load IDs of the sample articles
        List<Integer> sampleIds = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(WordFinderBenchmark.class.getResource("/es/bvalero/replacer/benchmark/sample-articles.txt").toURI()))) {
            stream.forEach(line -> sampleIds.add(Integer.valueOf(line.trim())));
        }

        // Load sample articles
        List<WikipediaPage> sampleContents = wikipediaService.getPagesByIds(sampleIds);

        // Load the finders
        List<WordAbstractFinder> finders = new ArrayList<>();
        /*
        // These finders are discarded as the times are about 100x slower compared with the other strategy
        finders.add(new WordIndexOfFinder(this.words));
        finders.add(new WordRegexFinder(this.words));
        // finders.add(new WordAutomatonFinder(this.words)); // Discarded: we need to increase too much the heap size
        finders.add(new WordRegexCompleteFinder(this.words));
        finders.add(new WordAlternateRegexFinder(this.words));
        // finders.add(new WordAlternateAutomatonFinder(this.words)); // Discarded: we need to increase too much the stack size
        finders.add(new WordAlternateRegexCompleteFinder(this.words));
        */
        finders.add(new WordRegexAllFinder(words));
        finders.add(new WordAutomatonAllFinder(words)); // WINNER
        finders.add(new WordRegexAllPossessiveFinder(words));
        finders.add(new WordRegexAllCompleteFinder(words));
        finders.add(new WordRegexAllCompletePossessiveFinder(words));

        System.out.println();
        System.out.println("FINDER\tTIME");
        sampleContents.forEach(value -> {
            for (WordAbstractFinder finder : finders) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < ITERATIONS; i++) {
                    finder.findMatches(value.getContent());
                }
                long end = System.currentTimeMillis() - start;
                System.out.println(finder.getClass().getSimpleName() + "\t" + end);
            }
        });

        Assert.assertTrue(true);
    }

}
