package es.bvalero.replacer.finder.benchmark.person;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.BenchmarkResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PersonFinderTest {

    private Collection<String> words;
    private String text;
    private Set<BenchmarkResult> expected;

    @BeforeEach
    public void setUp() {
        this.words = Set.of("Sky", "Julio", "Los Angeles", "Tokyo");

        this.text = "En Sky News, Julio Álvarez, Los Angeles Lakers, Tokyo TV, José Julio García.";

        this.expected = new HashSet<>();
        this.expected.add(BenchmarkResult.of(3, "Sky"));
        this.expected.add(BenchmarkResult.of(13, "Julio"));
        this.expected.add(BenchmarkResult.of(28, "Los Angeles"));
        this.expected.add(BenchmarkResult.of(48, "Tokyo"));
        this.expected.add(BenchmarkResult.of(63, "Julio"));
    }

    @Test
    void testPersonIndexOfFinder() {
        PersonIndexOfFinder finder = new PersonIndexOfFinder(words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonRegexFinder() {
        PersonRegexFinder finder = new PersonRegexFinder(words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonAutomatonFinder() {
        PersonAutomatonFinder finder = new PersonAutomatonFinder(words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonRegexCompleteFinder() {
        PersonRegexCompleteFinder finder = new PersonRegexCompleteFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonAutomatonCompleteFinder() {
        PersonAutomatonCompleteFinder finder = new PersonAutomatonCompleteFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonRegexAlternateFinder() {
        PersonRegexAlternateFinder finder = new PersonRegexAlternateFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonAutomatonAlternateFinder() {
        PersonAutomatonAlternateFinder finder = new PersonAutomatonAlternateFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonRegexAlternateCompleteFinder() {
        PersonRegexAlternateCompleteFinder finder = new PersonRegexAlternateCompleteFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonAutomatonAlternateCompleteFinder() {
        PersonAutomatonAlternateCompleteFinder finder = new PersonAutomatonAlternateCompleteFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonAhoCorasickFinder() {
        PersonAhoCorasickFinder finder = new PersonAhoCorasickFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonAhoCorasickLongestFinder() {
        PersonAhoCorasickLongestFinder finder = new PersonAhoCorasickLongestFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonAhoCorasickWholeLongestFinder() {
        PersonAhoCorasickWholeLongestFinder finder = new PersonAhoCorasickWholeLongestFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }
}
