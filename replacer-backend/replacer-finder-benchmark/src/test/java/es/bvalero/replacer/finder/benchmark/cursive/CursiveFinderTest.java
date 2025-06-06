package es.bvalero.replacer.finder.benchmark.cursive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CursiveFinderTest {

    private String text;
    private Set<BenchmarkResult> expected;

    @BeforeEach
    public void setUp() {
        String cursive1 = "''cursive1''";
        String cursive2 = "''cursive2''";
        String cursive3 = "''cursive3\n";
        String cursive4 = "''A '''''bold''''' inside''";
        String cursive5 = "''Beefeater's cool''"; // Only one letter after the inner quote not matched
        // We need more than one character between occurrences
        this.text = String.format("A %s - %s - %s - %s - %s.", cursive1, cursive2, cursive3, cursive4, cursive5);

        this.expected = new HashSet<>();
        this.expected.add(BenchmarkResult.of(2, cursive1));
        this.expected.add(BenchmarkResult.of(17, cursive2));
        this.expected.add(BenchmarkResult.of(32, cursive3));
        this.expected.add(BenchmarkResult.of(46, cursive4));
        this.expected.add(BenchmarkResult.of(76, cursive5));
    }

    @Test
    void testCursiveRegexDotLazyFinder() {
        CursiveRegexDotLazyFinder finder = new CursiveRegexDotLazyFinder();
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testCursiveRegexFinder() {
        CursiveRegexFinder finder = new CursiveRegexFinder();
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testCursiveRegexDotAllLookFinder() {
        CursiveRegexDotAllLookFinder finder = new CursiveRegexDotAllLookFinder();
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testCursiveRegexLookFinder() {
        CursiveRegexLookFinder finder = new CursiveRegexLookFinder();
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testCursiveAutomatonFinder() {
        CursiveAutomatonFinder finder = new CursiveAutomatonFinder();
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testCursiveLinearFinder() {
        CursiveLinearFinder finder = new CursiveLinearFinder();
        assertEquals(expected, finder.findMatches(text));
    }
}
