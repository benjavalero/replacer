package es.bvalero.replacer.finder.benchmark.word;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WordFinderTest {

    private Collection<String> words;
    private String text;
    private final Set<BenchmarkResult> expected = new HashSet<>();

    @BeforeEach
    public void setUp() {
        this.words = Set.of("A", "word", "eñe", "pa", "to", "la", "ta", "ma", "es", "so", "lo", "ti");
        this.text = "A word eñe 123 pa/to la_ta ma'es so-lo ti.";

        this.expected.add(BenchmarkResult.of(0, "A"));
        this.expected.add(BenchmarkResult.of(2, "word"));
        this.expected.add(BenchmarkResult.of(7, "eñe")); // non-Latin characters
        // Don't capture numbers even if included in the list
        this.expected.add(BenchmarkResult.of(15, "pa"));
        this.expected.add(BenchmarkResult.of(18, "to"));
        // Invalid right separator _
        // Invalid left separator _
        this.expected.add(BenchmarkResult.of(27, "ma"));
        this.expected.add(BenchmarkResult.of(30, "es"));
        this.expected.add(BenchmarkResult.of(33, "so"));
        this.expected.add(BenchmarkResult.of(36, "lo"));
        this.expected.add(BenchmarkResult.of(39, "ti"));
    }

    // Loop over all the words, find them in the text with a regex, and check they are complete in the text.
    @Test
    void testWordRegexFinder() {
        WordRegexFinder finder = new WordRegexFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    // Loop over all the words, find them in the text with a regex with boundaries, and check they are complete in the text.
    @Test
    void testWordRegexCompleteFinder() {
        WordRegexCompleteFinder finder = new WordRegexCompleteFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    // Loop over all the words, find them in the text with a regex with separators. No need to check they are complete in the text.
    @Test
    void testWordRegexCompleteSeparatorsFinder() {
        WordRegexCompleteSeparatorsFinder finder = new WordRegexCompleteSeparatorsFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    /* Alternative using a regex containing an alternation of all the words to find */

    @Test
    void testWordRegexAlternateFinder() {
        WordRegexAlternateFinder finder = new WordRegexAlternateFinder(this.words);
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

    /* Alternative finding all words in the text and then check if they are misspellings and (if needed) if they are complete. */

    @Test
    void testWordRegexAllFinder() {
        WordRegexAllFinder finder = new WordRegexAllFinder(this.words);
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

    /* Alternatives with an automaton. We cannot use elaborated regular expressions. */

    @Test
    void testWordAutomatonFinder() {
        WordAutomatonFinder finder = new WordAutomatonFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAutomatonAlternateFinder() {
        WordAutomatonAlternateFinder finder = new WordAutomatonAlternateFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAutomatonAllFinder() {
        WordAutomatonAllFinder finder = new WordAutomatonAllFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    /* Alternative using a linear-handcrafted approach */

    @Test
    void testWordLinearFinder() {
        WordLinearFinder finder = new WordLinearFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordLinearAllFinder() {
        WordLinearAllFinder finder = new WordLinearAllFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    /* Aho-Corasick */

    // Find all matches of all strings. Possibly overlapping.
    @Test
    void testWordAhoCorasickFinder() {
        WordAhoCorasickFinder finder = new WordAhoCorasickFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    // Find the left-most non-overlapping matches
    @Test
    void testWordAhoCorasickLongestFinder() {
        WordAhoCorasickLongestFinder finder = new WordAhoCorasickLongestFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    // Find only whole matches
    @Test
    void testWordAhoCorasickWholeFinder() {
        WordAhoCorasickWholeFinder finder = new WordAhoCorasickWholeFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    // Same as previous but admitting non-word characters. Possibly overlapping.
    @Test
    void testWordAhoCorasickWholeLongestFinder() {
        WordAhoCorasickWholeLongestFinder finder = new WordAhoCorasickWholeLongestFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    // Find all matches of all strings. Possibly overlapping.
    @Test
    void testWordTrieFinder() {
        WordTrieFinder finder = new WordTrieFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    // Find the left-most non-overlapping matches
    @Test
    void testWordTrieNoOverlappingFinder() {
        WordTrieNoOverlappingFinder finder = new WordTrieNoOverlappingFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    // Find only whole matches. Possibly overlapping.
    @Test
    void testWordTrieWholeFinder() {
        WordTrieWholeFinder finder = new WordTrieWholeFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }
}
