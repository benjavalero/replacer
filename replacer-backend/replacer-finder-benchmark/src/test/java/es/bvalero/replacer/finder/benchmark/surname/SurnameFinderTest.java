package es.bvalero.replacer.finder.benchmark.surname;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.BenchmarkResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SurnameFinderTest {

    private Collection<String> words;
    private String text;
    private Set<BenchmarkResult> expected;

    @BeforeEach
    public void setUp() {
        this.words = Set.of("Online", "Records", "de Verano", "Pinto");

        this.text = "En News Online, Álvaro Pinto, Victor Records, Juegos Olímpicos de Verano, Juan Pintor.";

        this.expected = new HashSet<>();
        this.expected.add(BenchmarkResult.of(8, "Online"));
        this.expected.add(BenchmarkResult.of(23, "Pinto"));
        this.expected.add(BenchmarkResult.of(37, "Records"));
        this.expected.add(BenchmarkResult.of(63, "de Verano"));
    }

    @Test
    void testSurnameIndexOfFinder() {
        SurnameIndexOfFinder finder = new SurnameIndexOfFinder(words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameRegexFinder() {
        SurnameRegexFinder finder = new SurnameRegexFinder(words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameAutomatonFinder() {
        SurnameAutomatonFinder finder = new SurnameAutomatonFinder(words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameRegexCompleteFinder() {
        SurnameRegexCompleteFinder finder = new SurnameRegexCompleteFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameAutomatonCompleteFinder() {
        SurnameAutomatonCompleteFinder finder = new SurnameAutomatonCompleteFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameRegexAlternateFinder() {
        SurnameRegexAlternateFinder finder = new SurnameRegexAlternateFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameAutomatonAlternateFinder() {
        SurnameAutomatonAlternateFinder finder = new SurnameAutomatonAlternateFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameRegexAlternateCompleteFinder() {
        SurnameRegexAlternateCompleteFinder finder = new SurnameRegexAlternateCompleteFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameAutomatonAlternateCompleteFinder() {
        SurnameAutomatonAlternateCompleteFinder finder = new SurnameAutomatonAlternateCompleteFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameAhoCorasickFinder() {
        SurnameAhoCorasickFinder finder = new SurnameAhoCorasickFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameAhoCorasickLongestFinder() {
        SurnameAhoCorasickLongestFinder finder = new SurnameAhoCorasickLongestFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameAhoCorasickWholeLongestFinder() {
        SurnameAhoCorasickWholeLongestFinder finder = new SurnameAhoCorasickWholeLongestFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }
}
