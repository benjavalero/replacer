package es.bvalero.replacer.finder.benchmark.word;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.Collection;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleMisspellingFinderTest {

    private Collection<String> words;
    private String text;
    private Set<BenchmarkResult> expected;

    @BeforeEach
    public void setUp() {
        // We can assume all words come from the simple listing and therefore only contain Spanish letters
        this.words = Set.of("a", "e", "ó", "ae", "nº");
        this.text = "a eñe ó a2 a_e a-e ae nº.";

        this.expected = Set.of(
            BenchmarkResult.of(0, "a"),
            // "ñ" is a word char
            BenchmarkResult.of(6, "ó"),
            // "a" in "a2" is not complete
            // "a" in "a_e" is not complete
            BenchmarkResult.of(15, "a"), // Dash separator to test Whole Aho-Corasick
            BenchmarkResult.of(17, "e"), // Dash separator to test Whole Aho-Corasick
            BenchmarkResult.of(19, "ae"),
            BenchmarkResult.of(22, "nº")
        );
    }

    @Test
    void testWordRegexFinder() {
        WordRegexFinder finder = new WordRegexFinder(this.words);
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
    void testWordRegexAllCompleteFinder() {
        WordRegexAllCompleteFinder finder = new WordRegexAllCompleteFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAllCompleteSeparatorsFinder() {
        WordRegexAllCompleteSeparatorsFinder finder = new WordRegexAllCompleteSeparatorsFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

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

    @Test
    void testWordAhoCorasickFinder() {
        WordAhoCorasickFinder finder = new WordAhoCorasickFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAhoCorasickLongestFinder() {
        WordAhoCorasickLongestFinder finder = new WordAhoCorasickLongestFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAhoCorasickWholeFinder() {
        WordAhoCorasickWholeFinder finder = new WordAhoCorasickWholeFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAhoCorasickWholeLongestFinder() {
        WordAhoCorasickWholeLongestFinder finder = new WordAhoCorasickWholeLongestFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordTrieFinder() {
        WordTrieFinder finder = new WordTrieFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordTrieNoOverlappingFinder() {
        WordTrieNoOverlappingFinder finder = new WordTrieNoOverlappingFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordTrieWholeFinder() {
        WordTrieWholeFinder finder = new WordTrieWholeFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }
}
