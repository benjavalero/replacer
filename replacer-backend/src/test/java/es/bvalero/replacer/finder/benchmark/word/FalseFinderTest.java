package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FalseFinderTest {
    private Collection<String> words;
    private String text;
    private Set<FinderResult> expected;

    @BeforeEach
    public void setUp() {
        this.words = Arrays.asList("Aaron Carter", "Victoria Abril");
        this.text = "En Abril Victoria Abril salió con Aaron Carter.";

        this.expected = new HashSet<>();
        this.expected.add(FinderResult.of(9, "Victoria Abril"));
        this.expected.add(FinderResult.of(34, "Aaron Carter"));
    }

    // NOTE: We can use the same finders that we use for misspellings just with a different set of words.
    // Nevertheless, some of them don't work as they search for simple words and not for composed ones.

    @Test
    void testWordIndexOfFinder() {
        WordIndexOfFinder finder = new WordIndexOfFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexFinder() {
        WordRegexFinder finder = new WordRegexFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAutomatonFinder() {
        WordAutomatonFinder finder = new WordAutomatonFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexCompleteFinder() {
        WordRegexCompleteFinder finder = new WordRegexCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAlternateFinder() {
        WordRegexAlternateFinder finder = new WordRegexAlternateFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAutomatonAlternateFinder() {
        WordAutomatonAlternateFinder finder = new WordAutomatonAlternateFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAlternateCompleteFinder() {
        WordRegexAlternateCompleteFinder finder = new WordRegexAlternateCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAllFinder() {
        WordRegexAllFinder finder = new WordRegexAllFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordAutomatonAllFinder() {
        WordAutomatonAllFinder finder = new WordAutomatonAllFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordLinearAllFinder() {
        WordLinearAllFinder finder = new WordLinearAllFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testWordRegexAllCompleteFinder() {
        WordRegexAllCompleteFinder finder = new WordRegexAllCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }
}
