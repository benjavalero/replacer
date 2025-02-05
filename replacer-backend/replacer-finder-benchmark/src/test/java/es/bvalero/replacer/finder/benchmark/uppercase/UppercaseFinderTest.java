package es.bvalero.replacer.finder.benchmark.uppercase;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UppercaseFinderTest {

    private Collection<String> words;
    private String text;
    private Set<BenchmarkResult> expected;

    @BeforeEach
    public void setUp() {
        this.words = Set.of("Enero", "Febrero", "Lunes", "Martes");
        this.text = "=Enero. Febrero, Lunes #  Martes.";

        this.expected = new HashSet<>();
        this.expected.add(BenchmarkResult.of(1, "Enero"));
        this.expected.add(BenchmarkResult.of(8, "Febrero"));
        this.expected.add(BenchmarkResult.of(26, "Martes"));
    }

    @Test
    void testUppercaseIndexOfFinder() {
        UppercaseIndexOfFinder finder = new UppercaseIndexOfFinder(words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testUppercaseAllWordsFinder() {
        UppercaseAllWordsFinder finder = new UppercaseAllWordsFinder(words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testUppercaseRegexIterateFinder() {
        UppercaseRegexIterateFinder finder = new UppercaseRegexIterateFinder(words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testUppercaseAutomatonIterateFinder() {
        UppercaseAutomatonIterateFinder finder = new UppercaseAutomatonIterateFinder(words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testUppercaseRegexLookBehindFinder() {
        UppercaseRegexLookBehindFinder finder = new UppercaseRegexLookBehindFinder(words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testUppercaseRegexAlternateFinder() {
        UppercaseRegexAlternateFinder finder = new UppercaseRegexAlternateFinder(words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testUppercaseAutomatonAlternateFinder() {
        UppercaseAutomatonAlternateFinder finder = new UppercaseAutomatonAlternateFinder(words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testUppercaseRegexAlternateLookBehindFinder() {
        UppercaseRegexAlternateLookBehindFinder finder = new UppercaseRegexAlternateLookBehindFinder(words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testUppercaseAutomatonAlternateAllFinder() {
        UppercaseAutomatonAlternateAllFinder finder = new UppercaseAutomatonAlternateAllFinder(words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testUppercaseAhoCorasickWholeFinder() {
        UppercaseAhoCorasickWholeFinder finder = new UppercaseAhoCorasickWholeFinder(words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testUppercaseAhoCorasickWholeLongestFinder() {
        UppercaseAhoCorasickWholeLongestFinder finder = new UppercaseAhoCorasickWholeLongestFinder(words);
        assertEquals(expected, finder.findMatches(text));
    }
}
