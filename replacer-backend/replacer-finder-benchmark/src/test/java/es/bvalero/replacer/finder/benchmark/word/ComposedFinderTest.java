package es.bvalero.replacer.finder.benchmark.word;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ComposedFinderTest {

    private Collection<String> words;
    private String text;
    private Set<BenchmarkResult> expected;

    @BeforeEach
    public void setUp() {
        this.words = Set.of(
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
        this.expected.add(BenchmarkResult.of(85, "1°"));
        this.expected.add(BenchmarkResult.of(99, "[[apartheid]]"));
    }

    // NOTE: We can use the same finders that we use for simple misspellings just with a different set of words,
    // except the finders finding all the words in the text as here we might search several words.

    @Test
    void testWordLinearFinder() {
        // It captures overlapping cases, but maybe we don't want that.
        WordLinearFinder finder = new WordLinearFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAutomatonAlternateFinder() {
        // It doesn't capture overlapping cases, complete or incomplete.
        WordAutomatonAlternateFinder finder = new WordAutomatonAlternateFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAhoCorasickFinder() {
        // It captures overlapping cases, but maybe we don't want that.
        // It doesn't capture whole words, so we need to check the completeness.
        WordAhoCorasickFinder finder = new WordAhoCorasickFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAhoCorasickLongestFinder() {
        // It doesn't capture overlapping cases, complete or incomplete.
        // It doesn't capture whole words, so we need to check the completeness.
        WordAhoCorasickLongestFinder finder = new WordAhoCorasickLongestFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAhoCorasickWholeLongestFinder() {
        // Known issue: it doesn't capture the character №
        // Known issue: it doesn't capture terms between brackets (hyperlinks)
        // It doesn't capture overlapping complete cases
        WordAhoCorasickWholeLongestFinder finder = new WordAhoCorasickWholeLongestFinder(this.words);
        assertEquals(expected, finder.findMatches(text));
    }
}
