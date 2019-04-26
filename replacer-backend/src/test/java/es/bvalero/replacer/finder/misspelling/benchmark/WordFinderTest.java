package es.bvalero.replacer.finder.misspelling.benchmark;

import es.bvalero.replacer.finder.misspelling.MisspellingManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class WordFinderTest {

    private final static int ITERATIONS = 5;
    private final static int RUNS = 10;

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
    public void testBenchmark() throws URISyntaxException, IOException {
        // Load the text of a medium article
        this.text = new String(Files.readAllBytes(Paths.get(WordFinderTest.class.getResource("/article-medium.txt").toURI())),
                StandardCharsets.UTF_8);

        // Load the misspellings
        this.words = new ArrayList<>();
        String misspellingText = new String(Files.readAllBytes(Paths.get(WordFinderTest.class.getResource("/misspelling-list.txt").toURI())),
                StandardCharsets.UTF_8);
        new MisspellingManager().parseMisspellingListText(misspellingText).forEach(misspelling -> {
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

        // Load the finders
        List<WordFinder> finders = new ArrayList<>();
        finders.add(new WordIndexOfFinder(this.words)); // 0.75 x WordMatchFinder
        finders.add(new WordMatchFinder(this.words)); // MUCH slower than the rest of finders, from WordMatchAllFinder.
        // finders.add(new WordAutomatonFinder(this.words)); // 1 x WordMatchFinder. To run this, we need to increase the heap.
        finders.add(new WordMatchCompleteFinder(this.words)); // 10 x WordMatchFinder
        finders.add(new WordRegexAlternateFinder(this.words)); // 4 x WordMatchFinder. To run this, we need to increase the stack: -Xss3m
        finders.add(new WordAutomatonAlternateFinder(this.words)); // 4 x WordMatchFinder
        finders.add(new WordRegexAlternateCompleteFinder(this.words)); // 1 x WordMatchFinder
        finders.add(new WordMatchAllFinder(this.words));
        finders.add(new WordAutomatonAllFinder(this.words));
        finders.add(new WordMatchAllPossessiveFinder(this.words));
        finders.add(new WordMatchAllCompleteFinder(this.words));
        finders.add(new WordMatchAllCompleteLazyFinder(this.words));
        finders.add(new WordMatchAllCompletePossessiveFinder(this.words));
        finders.add(new WordMatchDotAllCompleteLazyFinder(this.words));

        // When running the test several times for each finder, in general the first run is slower.
        // However for the rest of runs we find slow and fast runs with no special tendency.
        // In this case it would better to skip only the first run and extract statistics for all the rest.
        for (WordFinder finder : finders) {
            System.out.print(finder.getClass().getSimpleName() + "\t");
        }
        System.out.println();

        for (int n = 0; n <= RUNS; n++) {
            for (WordFinder finder : finders) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < ITERATIONS; i++) {
                    finder.findWords(this.text);
                }
                long end = System.currentTimeMillis() - start;
                System.out.print(end + "\t");
            }
            System.out.println();
        }
    }

}
