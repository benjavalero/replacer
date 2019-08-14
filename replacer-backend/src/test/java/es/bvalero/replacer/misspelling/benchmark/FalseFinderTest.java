package es.bvalero.replacer.misspelling.benchmark;

import es.bvalero.replacer.finder.MatchResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FalseFinderTest {

    private Collection<String> words;
    private String text;
    private Set<MatchResult> expected;

    @Before
    public void setUp() {
        this.words = Arrays.asList("Aaron Carter", "Victoria Abril");
        this.text = "En Abril Victoria Abril salió con Aaron Carter.";

        this.expected = new HashSet<>();
        this.expected.add(MatchResult.of(9, "Victoria Abril"));
        this.expected.add(MatchResult.of(34, "Aaron Carter"));
    }

    /* NOTE: We can use the same finders that we use for misspellings just with a different set of words */

    @Test
    public void testWordIndexOfFinder() {
        WordIndexOfFinder finder = new WordIndexOfFinder(this.words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testWordRegexFinder() {
        WordRegexFinder finder = new WordRegexFinder(this.words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testWordAutomatonFinder() {
        WordAutomatonFinder finder = new WordAutomatonFinder(this.words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testWordRegexCompleteFinder() {
        WordRegexCompleteFinder finder = new WordRegexCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testWordAlternateRegexFinder() {
        WordAlternateRegexFinder finder = new WordAlternateRegexFinder(this.words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testWordAlternateAutomatonFinder() {
        WordAlternateAutomatonFinder finder = new WordAlternateAutomatonFinder(this.words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testWordAlternateRegexCompleteFinder() {
        WordAlternateRegexCompleteFinder finder = new WordAlternateRegexCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

}
