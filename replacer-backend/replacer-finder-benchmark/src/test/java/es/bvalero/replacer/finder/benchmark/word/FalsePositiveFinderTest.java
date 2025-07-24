package es.bvalero.replacer.finder.benchmark.word;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
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
        this.words = Set.of(
            "t[ií]",
            "Top Album"
            // "Album Chart", // To test overlapping
            // "es aún", // To test incomplete overlapping
            // "aún son"
        )
            .stream()
            .flatMap(w -> FinderUtils.expandRegex(w).stream())
            .toList();
        this.text = "Para tí-tí. Top Album Chart, los ratones aún son roedores.";

        this.expected = new HashSet<>();
        this.expected.add(BenchmarkResult.of(5, "tí"));
        this.expected.add(BenchmarkResult.of(8, "tí"));
        this.expected.add(BenchmarkResult.of(12, "Top Album"));
        // this.expected.add(BenchmarkResult.of(16, "Album Chart")); // To test overlapping
        // this.expected.add(BenchmarkResult.of(41, "aún son"));
    }

    // NOTE: We cannot use the "all" approach as here we might search for several words.
    // For the same reason, we cannot use the Aho-Corasick "whole" algorithm as it doesn't allow non-word characters.

    @Test
    void testWordRegexFinder() {
        // In the case of overlapping, it finds all complete matches.
        WordRegexFinder finder = new WordRegexFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexCompleteFinder() {
        // In the case of overlapping, it finds all complete matches.
        WordRegexCompleteFinder finder = new WordRegexCompleteFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexCompleteSeparatorsFinder() {
        // In the case of overlapping, it finds all complete matches.
        WordRegexCompleteSeparatorsFinder finder = new WordRegexCompleteSeparatorsFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAlternateFinder() {
        // In the case of overlapping, it finds the first match, complete or incomplete.
        WordRegexAlternateFinder finder = new WordRegexAlternateFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAlternateCompleteFinder() {
        // In the case of overlapping, it finds the first complete match.
        WordRegexAlternateCompleteFinder finder = new WordRegexAlternateCompleteFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAlternateCompleteSeparatorsFinder() {
        // In the case of overlapping, it finds the first complete match.
        WordRegexAlternateCompleteSeparatorsFinder finder = new WordRegexAlternateCompleteSeparatorsFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAutomatonFinder() {
        // In the case of overlapping, it finds all complete matches.
        WordAutomatonFinder finder = new WordAutomatonFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAutomatonAlternateFinder() {
        // In the case of overlapping, it finds the first match, complete or incomplete.
        WordAutomatonAlternateFinder finder = new WordAutomatonAlternateFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordLinearFinder() {
        // In the case of overlapping, it finds all complete matches.
        WordLinearFinder finder = new WordLinearFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAhoCorasickFinder() {
        // In the case of overlapping, it finds all complete matches.
        WordAhoCorasickFinder finder = new WordAhoCorasickFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAhoCorasickLongestFinder() {
        // In the case of overlapping, it finds the first match, complete or incomplete.
        WordAhoCorasickLongestFinder finder = new WordAhoCorasickLongestFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAhoCorasickWholeLongestFinder() {
        // In the case of overlapping, it finds the first complete match.
        WordAhoCorasickWholeLongestFinder finder = new WordAhoCorasickWholeLongestFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordTrieFinder() {
        // In the case of overlapping, it finds all complete matches.
        WordTrieFinder finder = new WordTrieFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordTrieNoOverlappingFinder() {
        // In the case of overlapping, it finds the first complete match.
        WordTrieNoOverlappingFinder finder = new WordTrieNoOverlappingFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordTrieWholeFinder() {
        // In the case of overlapping, it finds all complete matches.
        WordTrieWholeFinder finder = new WordTrieWholeFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }
}
