package es.bvalero.replacer.finder.misspelling.benchmark;

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
        this.words = Arrays.asList("Um", "um", "españa", "m2");
        this.text = "Um suma um, españa um m2 España.";

        this.expected = new HashSet<>();
        this.expected.add(new WordMatch(0, "Um"));
        this.expected.add(new WordMatch(8, "um"));
        this.expected.add(new WordMatch(12, "españa"));
        this.expected.add(new WordMatch(19, "um"));
        this.expected.add(new WordMatch(22, "m2"));
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
    public void testFindWordMatchAllCompleteLazy() {
        WordMatchAllCompleteLazyFinder finder = new WordMatchAllCompleteLazyFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordMatchAllCompletePossessive() {
        WordMatchAllCompletePossessiveFinder finder = new WordMatchAllCompletePossessiveFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindWordMatchDotAllCompleteLazy() {
        WordMatchDotAllCompleteLazyFinder finder = new WordMatchDotAllCompleteLazyFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

}
