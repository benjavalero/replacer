package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WordFinderTest {
    private Collection<String> words;
    private String text;
    private Set<FinderResult> expected;

    @BeforeEach
    public void setUp() {
        // Besides the common misspellings the list includes some terms
        // with numbers, dashes, quotes, super-indices and dots.

        // Numbers: m2 km2 -> Finder for measure units
        // Final dots: hr. cm. -> Finder for measure units
        // Super-indices: n° nª -> Finder for measure units
        // Dashes: Castilla-León -> Covered by misspelling finder
        // Single quotes: dvd's cd's -> Covered by misspelling finder
        // Middle dots: E.E.U.U. -> Not covered

        this.words = Arrays.asList("Um", "um", "españa", "Castilla-León", "cd's");
        this.text = "Um suma um, españa um m2 España y Castilla-León + cd's.";

        this.expected = new HashSet<>();
        this.expected.add(FinderResult.of(0, "Um"));
        this.expected.add(FinderResult.of(8, "um"));
        this.expected.add(FinderResult.of(12, "españa"));
        this.expected.add(FinderResult.of(19, "um"));
        this.expected.add(FinderResult.of(34, "Castilla-León"));
        this.expected.add(FinderResult.of(50, "cd's"));
    }

    @Test
    public void testWordIndexOfFinder() {
        WordIndexOfFinder finder = new WordIndexOfFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testWordRegexFinder() {
        WordRegexFinder finder = new WordRegexFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testWordAutomatonFinder() {
        WordAutomatonFinder finder = new WordAutomatonFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testWordRegexCompleteFinder() {
        WordRegexCompleteFinder finder = new WordRegexCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testWordRegexAlternateFinder() {
        WordRegexAlternateFinder finder = new WordRegexAlternateFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testWordAutomatonAlternateFinder() {
        WordAutomatonAlternateFinder finder = new WordAutomatonAlternateFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testWordRegexAlternateCompleteFinder() {
        WordRegexAlternateCompleteFinder finder = new WordRegexAlternateCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testWordRegexAllFinder() {
        WordRegexAllFinder finder = new WordRegexAllFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testWordAutomatonAllFinder() {
        WordAutomatonAllFinder finder = new WordAutomatonAllFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testWordLinearAllFinder() {
        WordLinearAllFinder finder = new WordLinearAllFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testWordRegexAllCompleteFinder() {
        WordRegexAllCompleteFinder finder = new WordRegexAllCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }
}