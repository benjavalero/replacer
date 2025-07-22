package es.bvalero.replacer.finder.benchmark.word;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.Collection;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ComposedMisspellingFinderTest {

    private Collection<String> words;
    private String text;
    private Set<BenchmarkResult> expected;

    @BeforeEach
    public void setUp() {
        this.words = Set.of(
            "a",
            "b c",
            "d'e",
            "f-g",
            "h2",
            "í_j",
            "k,",
            "l.",
            "1º",
            "2ª",
            "3°",
            "№",
            "''m''",
            "[[n]]",
            "o p",
            "p q"
        );
        this.text = "a b c d'e f-g h2 í_j k, l. 1º 2ª 3° № ''m'' x[[n]]x o p qq";

        this.expected = Set.of(
            BenchmarkResult.of(0, "a"),
            BenchmarkResult.of(2, "b c"),
            BenchmarkResult.of(6, "d'e"),
            BenchmarkResult.of(10, "f-g"),
            BenchmarkResult.of(14, "h2"),
            BenchmarkResult.of(17, "í_j"),
            BenchmarkResult.of(21, "k,"),
            BenchmarkResult.of(24, "l."),
            BenchmarkResult.of(27, "1º"),
            BenchmarkResult.of(30, "2ª"),
            BenchmarkResult.of(33, "3°"),
            BenchmarkResult.of(36, "№"),
            BenchmarkResult.of(38, "''m''"),
            BenchmarkResult.of(45, "[[n]]"),
            BenchmarkResult.of(52, "o p")
        );
    }

    // NOTE: We cannot use the "all" approach as here we might search for several words.
    // For the same reason, we cannot use the Aho-Corasick "whole" algorithm as it doesn't allow non-word characters.
    // The "complete" approach with \\b separator doesn't work for expressions ending with dots or commas
    // If "complete separator" and "whole" approaches don't work when the "word" is surrounded by letters.

    @Test
    void testWordRegexFinder() {
        // In the case of overlapping, it finds both matches.
        WordRegexFinder finder = new WordRegexFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAlternateFinder() {
        // In the case of overlapping, it finds the first match.
        // But if the first match is not complete, it hides the second one.
        WordRegexAlternateFinder finder = new WordRegexAlternateFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAutomatonFinder() {
        // In the case of overlapping, it finds both matches.
        WordAutomatonFinder finder = new WordAutomatonFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAutomatonAlternateFinder() {
        // In the case of overlapping, it finds the first match.
        // But if the first match is not complete, it hides the second one.
        WordAutomatonAlternateFinder finder = new WordAutomatonAlternateFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordLinearFinder() {
        // In the case of overlapping, it finds both matches.
        WordLinearFinder finder = new WordLinearFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAhoCorasickFinder() {
        // In the case of overlapping, it finds both matches.
        WordAhoCorasickFinder finder = new WordAhoCorasickFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAhoCorasickLongestFinder() {
        // In the case of overlapping, it finds the first match.
        // But if the first match is not complete, it hides the second one.
        WordAhoCorasickLongestFinder finder = new WordAhoCorasickLongestFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordTrieFinder() {
        // In the case of overlapping, it finds both matches.
        WordTrieFinder finder = new WordTrieFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordTrieNoOverlappingFinder() {
        // In the case of overlapping, it finds the first match.
        // But if the first match is not complete, it hides the second one.
        WordTrieNoOverlappingFinder finder = new WordTrieNoOverlappingFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }
}
