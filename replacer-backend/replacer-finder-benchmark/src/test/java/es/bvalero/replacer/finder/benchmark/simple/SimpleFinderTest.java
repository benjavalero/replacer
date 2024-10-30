package es.bvalero.replacer.finder.benchmark.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleFinderTest {

    private String text;
    private final String word = "_";
    private final Set<BenchmarkResult> expected = new HashSet<>();

    @BeforeEach
    public void setUp() {
        this.text = "In_the_town_where_I_was_born_lived_a_man_who_sailed_the_sea.";

        this.expected.add(BenchmarkResult.of(2, word));
        this.expected.add(BenchmarkResult.of(6, word));
        this.expected.add(BenchmarkResult.of(11, word));
        this.expected.add(BenchmarkResult.of(17, word));
        this.expected.add(BenchmarkResult.of(19, word));
        this.expected.add(BenchmarkResult.of(23, word));
        this.expected.add(BenchmarkResult.of(28, word));
        this.expected.add(BenchmarkResult.of(34, word));
        this.expected.add(BenchmarkResult.of(36, word));
        this.expected.add(BenchmarkResult.of(40, word));
        this.expected.add(BenchmarkResult.of(44, word));
        this.expected.add(BenchmarkResult.of(51, word));
        this.expected.add(BenchmarkResult.of(55, word));
    }

    @Test
    void testSimpleLinearFinder() {
        SimpleLinearFinder finder = new SimpleLinearFinder(word);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSimpleRegexFinder() {
        SimpleRegexFinder finder = new SimpleRegexFinder(word);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSimpleAutomatonFinder() {
        SimpleAutomatonFinder finder = new SimpleAutomatonFinder(word);
        assertEquals(expected, finder.findMatches(text));
    }
}
