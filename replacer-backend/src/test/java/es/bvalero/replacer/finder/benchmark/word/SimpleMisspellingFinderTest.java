package es.bvalero.replacer.finder.benchmark.word;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleMisspellingFinderTest {

    private Collection<String> words;
    private String text;
    private Set<BenchmarkResult> expected;

    @BeforeEach
    public void setUp() {
        this.words = Set.of("Um", "um", "españa");
        this.text = "Um suma um, españa um.españa ";

        this.expected = new HashSet<>();
        this.expected.add(BenchmarkResult.of(0, "Um"));
        this.expected.add(BenchmarkResult.of(8, "um"));
        this.expected.add(BenchmarkResult.of(12, "españa"));
        this.expected.add(BenchmarkResult.of(19, "um"));
    }

    @Test
    void testWordLinearLoopFinder() {
        WordLinearFinder finder = new WordLinearFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexFinder() {
        WordRegexFinder finder = new WordRegexFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAutomatonFinder() {
        WordAutomatonFinder finder = new WordAutomatonFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexCompleteFinder() {
        WordRegexCompleteFinder finder = new WordRegexCompleteFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexCompleteSeparatorsFinder() {
        WordRegexCompleteSeparatorsFinder finder = new WordRegexCompleteSeparatorsFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAlternateFinder() {
        WordRegexAlternateFinder finder = new WordRegexAlternateFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAutomatonAlternateFinder() {
        WordAutomatonAlternateFinder finder = new WordAutomatonAlternateFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAlternateCompleteFinder() {
        WordRegexAlternateCompleteFinder finder = new WordRegexAlternateCompleteFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAlternateCompleteSeparatorsFinder() {
        WordRegexAlternateCompleteSeparatorsFinder finder = new WordRegexAlternateCompleteSeparatorsFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAllFinder() {
        WordRegexAllFinder finder = new WordRegexAllFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAutomatonAllFinder() {
        WordAutomatonAllFinder finder = new WordAutomatonAllFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordLinearAllFinder() {
        WordLinearAllFinder finder = new WordLinearAllFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAllCompleteFinder() {
        WordRegexAllCompleteFinder finder = new WordRegexAllCompleteFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAllCompleteSeparatorsFinder() {
        WordRegexAllCompleteSeparatorsFinder finder = new WordRegexAllCompleteSeparatorsFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }
}
