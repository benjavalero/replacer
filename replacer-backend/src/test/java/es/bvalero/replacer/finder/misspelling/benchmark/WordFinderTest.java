package es.bvalero.replacer.finder.misspelling.benchmark;

import es.bvalero.replacer.authentication.AuthenticationService;
import es.bvalero.replacer.finder.misspelling.MisspellingManager;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaFacade;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.junit.Assert;
import org.junit.Before;
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
import java.util.*;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {MisspellingManager.class, WikipediaService.class, WikipediaFacade.class, AuthenticationService.class},
        initializers = ConfigFileApplicationContextInitializer.class)
public class WordFinderTest {

    private final static int ITERATIONS = 100000;

    @Autowired
    private MisspellingManager misspellingManager;

    @Autowired
    private WikipediaService wikipediaService;

    private Collection<String> words;
    private String text;
    private Set<WordMatch> expected;

    @Before
    public void setUp() {
        this.words = Arrays.asList("Um", "um", "españa", "m2");
        this.text = "Um suma um, españa um m2 España.";

        this.expected = new HashSet<>();
        this.expected.add(new WordMatch(0, "Um"));
        this.expected.add(new WordMatch(8, "um"));
        this.expected.add(new WordMatch(12, "españa"));
        this.expected.add(new WordMatch(19, "um"));
        this.expected.add(new WordMatch(22, "m2"));
    }

    @Test
    public void testFindWordIndexOf() {
        WordIndexOfFinder finder = new WordIndexOfFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordMatch() {
        WordMatchFinder finder = new WordMatchFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordAutomaton() {
        WordAutomatonFinder finder = new WordAutomatonFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordMatchComplete() {
        WordMatchCompleteFinder finder = new WordMatchCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordRegexAlternate() {
        WordRegexAlternateFinder finder = new WordRegexAlternateFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordAutomatonAlternate() {
        WordAutomatonAlternateFinder finder = new WordAutomatonAlternateFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordRegexAlternateComplete() {
        WordRegexAlternateCompleteFinder finder = new WordRegexAlternateCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordMatchAll() {
        WordMatchAllFinder finder = new WordMatchAllFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordAutomatonAll() {
        WordAutomatonAllFinder finder = new WordAutomatonAllFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordMatchAllPossessive() {
        WordMatchAllPossessiveFinder finder = new WordMatchAllPossessiveFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordMatchAllComplete() {
        WordMatchAllCompleteFinder finder = new WordMatchAllCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordMatchAllCompleteLazy() {
        WordMatchAllCompleteLazyFinder finder = new WordMatchAllCompleteLazyFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordMatchAllCompletePossessive() {
        WordMatchAllCompletePossessiveFinder finder = new WordMatchAllCompletePossessiveFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordMatchDotAllCompleteLazy() {
        WordMatchDotAllCompleteLazyFinder finder = new WordMatchDotAllCompleteLazyFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    // @Ignore
    public void testBenchmark() throws IOException, WikipediaException, URISyntaxException {
        // Load the misspellings
        this.words = new ArrayList<>();
        this.misspellingManager.findWikipediaMisspellings().forEach(misspelling -> {
            String word = misspelling.getWord();
            if (misspelling.isCaseSensitive()) {
                this.words.add(word);
            } else {
                // If case-insensitive, we add to the map "[wW]ord".
                String firstLetter = word.substring(0, 1);
                String newWord = '[' + firstLetter + firstLetter.toUpperCase(Locale.forLanguageTag("es")) + ']' + word.substring(1);
                this.words.add(newWord);
            }
        });

        // Load IDs of the sample articles
        List<Integer> sampleIds = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(WordFinderTest.class.getResource("/benchmark/sample-articles.txt").toURI()))) {
            stream.forEach(line -> sampleIds.add(Integer.valueOf(line.trim())));
        }

        // Load sample articles
        Map<Integer, String> sampleContents = wikipediaService.getPagesContent(sampleIds, null);

        // Load the finders
        List<WordFinder> finders = new ArrayList<>();
        // finders.add(new WordIndexOfFinder(this.words)); // 0.75 x WordMatchFinder
        // finders.add(new WordMatchFinder(this.words)); // MUCH slower than the rest of finders, from WordMatchAllFinder.
        // finders.add(new WordAutomatonFinder(this.words)); // 1 x WordMatchFinder. To run this, we need to increase the heap.
        // finders.add(new WordMatchCompleteFinder(this.words)); // 10 x WordMatchFinder
        // finders.add(new WordRegexAlternateFinder(this.words)); // 4 x WordMatchFinder. To run this, we need to increase the stack: -Xss3m
        // finders.add(new WordAutomatonAlternateFinder(this.words)); // 4 x WordMatchFinder
        // finders.add(new WordRegexAlternateCompleteFinder(this.words)); // 1 x WordMatchFinder
        finders.add(new WordMatchAllFinder(this.words));
        finders.add(new WordAutomatonAllFinder(this.words));
        finders.add(new WordMatchAllPossessiveFinder(this.words));
        finders.add(new WordMatchAllCompleteFinder(this.words));
        finders.add(new WordMatchAllCompleteLazyFinder(this.words));
        finders.add(new WordMatchAllCompletePossessiveFinder(this.words));
        finders.add(new WordMatchDotAllCompleteLazyFinder(this.words));

        System.out.println();
        System.out.println("FINDER\tTIME");
        sampleContents.forEach((key, value) -> {
            for (WordFinder finder : finders) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < ITERATIONS; i++) {
                    finder.findWords(this.text);
                }
                long end = System.currentTimeMillis() - start;
                System.out.println(finder.getClass().getSimpleName() + "\t" + end);
            }
        });
    }

}
