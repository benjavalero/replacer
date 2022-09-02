package es.bvalero.replacer.finder.benchmark.simple;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleFinderTest {

    private String text;
    private Set<BenchmarkResult> expected;

    @BeforeEach
    public void setUp() {
        this.text = "In the town where I was born lived a man who sailed the sea.";

        this.expected = new HashSet<>();
        this.expected.add(BenchmarkResult.of(2, SPACE));
        this.expected.add(BenchmarkResult.of(6, SPACE));
        this.expected.add(BenchmarkResult.of(11, SPACE));
        this.expected.add(BenchmarkResult.of(17, SPACE));
        this.expected.add(BenchmarkResult.of(19, SPACE));
        this.expected.add(BenchmarkResult.of(23, SPACE));
        this.expected.add(BenchmarkResult.of(28, SPACE));
        this.expected.add(BenchmarkResult.of(34, SPACE));
        this.expected.add(BenchmarkResult.of(36, SPACE));
        this.expected.add(BenchmarkResult.of(40, SPACE));
        this.expected.add(BenchmarkResult.of(44, SPACE));
        this.expected.add(BenchmarkResult.of(51, SPACE));
        this.expected.add(BenchmarkResult.of(55, SPACE));
    }

    @Test
    void testCursiveRegexFinder() {
        SimpleRegexFinder finder = new SimpleRegexFinder();
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testCursiveAutomatonFinder() {
        SimpleAutomatonFinder finder = new SimpleAutomatonFinder();
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testCursiveLinearFinder() {
        SimpleLinearFinder finder = new SimpleLinearFinder();
        assertEquals(expected, finder.findMatches(text));
    }
}
