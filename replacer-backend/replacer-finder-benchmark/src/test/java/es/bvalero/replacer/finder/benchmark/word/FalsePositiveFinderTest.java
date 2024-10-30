package es.bvalero.replacer.finder.benchmark.word;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FalsePositiveFinderTest {

    private Collection<String> words;
    private String text;
    private Set<BenchmarkResult> expected;

    @BeforeEach
    public void setUp() {
        this.words = Set.of("tí", "Top Album", "Album Chart", "es aún", "aún son");
        this.text = "Para tí-tí un tío. Top Album Chart, los ratones aún son roedores.";

        this.expected = new HashSet<>();
        this.expected.add(BenchmarkResult.of(5, "tí"));
        this.expected.add(BenchmarkResult.of(8, "tí"));
        this.expected.add(BenchmarkResult.of(19, "Top Album"));
        this.expected.add(BenchmarkResult.of(23, "Album Chart"));
        this.expected.add(BenchmarkResult.of(48, "aún son"));
    }

    // NOTE: We can use the same finders that we use for simple misspellings just with a different set of words,
    // except the finders finding all the words in the text as here we might search several words.

    @Test
    void testWordLinearFinder() {
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
    void testWordRegexAlternateFinder() {
        // Known issue. It doesn't capture overlapping cases, complete or incomplete.
        WordRegexAlternateFinder finder = new WordRegexAlternateFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAutomatonAlternateFinder() {
        // Known issue. It doesn't capture overlapping cases, complete or incomplete.
        WordAutomatonAlternateFinder finder = new WordAutomatonAlternateFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAlternateCompleteFinder() {
        // Known issue. It doesn't capture overlapping complete cases.
        WordRegexAlternateCompleteFinder finder = new WordRegexAlternateCompleteFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAhoCorasickFinder() {
        WordAhoCorasickFinder finder = new WordAhoCorasickFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAhoCorasickLongestFinder() {
        // Known issue. It doesn't capture overlapping cases, complete or incomplete.
        WordAhoCorasickLongestFinder finder = new WordAhoCorasickLongestFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAhoCorasickWholeLongestFinder() {
        // Known issue. It doesn't capture overlapping complete cases.
        WordAhoCorasickWholeLongestFinder finder = new WordAhoCorasickWholeLongestFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }
}
