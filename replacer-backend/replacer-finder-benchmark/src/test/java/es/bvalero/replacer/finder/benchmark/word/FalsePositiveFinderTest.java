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
    // We exclude the ones not capturing overlaps, in particular the "alternate" approaches.

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

    /* Alternatives with an automaton. We cannot use elaborated regular expressions. */

    @Test
    void testWordAutomatonFinder() {
        WordAutomatonFinder finder = new WordAutomatonFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    /* Alternative using a linear-handcrafted approach */

    @Test
    void testWordLinearFinder() {
        WordLinearFinder finder = new WordLinearFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    /* Aho-Corasick. Only overlapping with non-word characters. */

    // Find all matches of all strings. Possibly overlapping.
    @Test
    void testWordAhoCorasickFinder() {
        WordAhoCorasickFinder finder = new WordAhoCorasickFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }
}
