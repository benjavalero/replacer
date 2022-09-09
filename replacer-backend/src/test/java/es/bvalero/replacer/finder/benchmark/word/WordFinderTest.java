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
        this.words = Set.of("A", "word", "eñe", "123", "pa", "to", "la", "ta", "ma", "es", "so", "lo", "ti");
        this.text = "A word eñe 123 pa/to la_ta ma'es so-lo ti.";

        this.expected.add(BenchmarkResult.of(0, "A"));
        this.expected.add(BenchmarkResult.of(2, "word"));
        this.expected.add(BenchmarkResult.of(7, "eñe")); // non-Latin characters
        // Don't capture numbers even if included in the list
        // Invalid right separator /
        // Invalid left separator /
        // Invalid right separator _
        // Invalid left separator _
        this.expected.add(BenchmarkResult.of(27, "ma"));
        // Preceded by apostrophe
        this.expected.add(BenchmarkResult.of(33, "so"));
        this.expected.add(BenchmarkResult.of(36, "lo"));
        this.expected.add(BenchmarkResult.of(39, "ti"));
    }

    @Test
    void testWordLinearAllFinder() {
        WordLinearAllFinder finder = new WordLinearAllFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAllFinder() {
        WordRegexAllFinder finder = new WordRegexAllFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAutomatonAllFinder() {
        WordAutomatonAllFinder finder = new WordAutomatonAllFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAllCompleteFinder() {
        WordRegexAllCompleteFinder finder = new WordRegexAllCompleteFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }
}
