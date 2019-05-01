package es.bvalero.replacer.misspelling;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class WordFinderTest {

    private Collection<String> words;
    private String text;
    private Set<WordMatch> expected;

    @Before
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
        this.expected.add(new WordMatch(0, "Um"));
        this.expected.add(new WordMatch(8, "um"));
        this.expected.add(new WordMatch(12, "españa"));
        this.expected.add(new WordMatch(19, "um"));
        this.expected.add(new WordMatch(34, "Castilla-León"));
        this.expected.add(new WordMatch(50, "cd's"));
    }

    @Test
    public void testFindWordIndexOf() {
        WordIndexOfFinder finder = new WordIndexOfFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordMatch() {
        WordMatchFinder finder = new WordMatchFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordAutomaton() {
        WordAutomatonFinder finder = new WordAutomatonFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordMatchComplete() {
        WordMatchCompleteFinder finder = new WordMatchCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordRegexAlternate() {
        WordRegexAlternateFinder finder = new WordRegexAlternateFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordAutomatonAlternate() {
        WordAutomatonAlternateFinder finder = new WordAutomatonAlternateFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordRegexAlternateComplete() {
        WordRegexAlternateCompleteFinder finder = new WordRegexAlternateCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordMatchAll() {
        WordMatchAllFinder finder = new WordMatchAllFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordAutomatonAll() {
        WordAutomatonAllFinder finder = new WordAutomatonAllFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordMatchAllPossessive() {
        WordMatchAllPossessiveFinder finder = new WordMatchAllPossessiveFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordMatchAllComplete() {
        WordMatchAllCompleteFinder finder = new WordMatchAllCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordMatchAllCompletePossessive() {
        WordMatchAllCompletePossessiveFinder finder = new WordMatchAllCompletePossessiveFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

}
