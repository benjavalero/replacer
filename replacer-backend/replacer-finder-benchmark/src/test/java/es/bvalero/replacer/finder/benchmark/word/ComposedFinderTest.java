package es.bvalero.replacer.finder.benchmark.word;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ComposedFinderTest {

    private Collection<String> words;
    private String text;
    private Set<BenchmarkResult> expected;

    @BeforeEach
    public void setUp() {
        this.words = List.of(
            "a el",
            "ad honorem",
            "ad-hoc",
            "a. m.",
            "C's",
            "hm2",
            "№",
            "1º",
            "2ª",
            "al al",
            "un un",
            "1°",
            "[[apartheid]]"
        );
        this.text =
            "A el ad honorem ad-hoc a 3 a. m. en C's. de hm2. el № 1º en 2ª tal al un unicornio y 1° con enlace [[apartheid]].";

        this.expected = new HashSet<>();
        this.expected.add(BenchmarkResult.of(5, "ad honorem"));
        this.expected.add(BenchmarkResult.of(16, "ad-hoc"));
        this.expected.add(BenchmarkResult.of(27, "a. m."));
        this.expected.add(BenchmarkResult.of(36, "C's"));
        this.expected.add(BenchmarkResult.of(44, "hm2"));
        this.expected.add(BenchmarkResult.of(52, "№"));
        this.expected.add(BenchmarkResult.of(54, "1º"));
        this.expected.add(BenchmarkResult.of(60, "2ª"));
        this.expected.add(BenchmarkResult.of(60, "2ª"));
        this.expected.add(BenchmarkResult.of(85, "1°")); // Degree
        this.expected.add(BenchmarkResult.of(99, "[[apartheid]]"));
    }

    // NOTE: We can use the same finders that we use for simple misspellings just with a different set of words,
    // except the finders finding all the words in the text as here we might search several words.
    // The "complete" approaches don't work as the regular expression detects some non-word characters as word boundaries.

    // Loop over all the words, find them in the text with a regex, and check they are complete in the text.
    @Test
    void testWordRegexFinder() {
        WordRegexFinder finder = new WordRegexFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    // Loop over all the words, find them in the text with a regex with boundaries, and check they are complete in the text.

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
    void testWordRegexAlternateCompleteSeparatorsFinder() {
        WordRegexAlternateCompleteSeparatorsFinder finder = new WordRegexAlternateCompleteSeparatorsFinder(this.words);
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

    /* Alternative using a linear-handcrafted approach */

    @Test
    void testWordLinearFinder() {
        WordLinearFinder finder = new WordLinearFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    /* Aho-Corasick. Only non-overlapping. */

    // Find the left-most non-overlapping matches
    @Test
    void testWordAhoCorasickLongestFinder() {
        WordAhoCorasickLongestFinder finder = new WordAhoCorasickLongestFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }
}
