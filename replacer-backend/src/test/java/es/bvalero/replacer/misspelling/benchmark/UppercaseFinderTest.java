package es.bvalero.replacer.misspelling.benchmark;

import es.bvalero.replacer.finder.IgnoredReplacement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UppercaseFinderTest {

    private Collection<String> words;
    private String text;
    private Set<IgnoredReplacement> expected;

    @Before
    public void setUp() {
        this.words = Arrays.asList("Enero", "Febrero", "Lunes", "Martes");
        this.text = "=Enero. Febrero, Lunes #  Martes.";

        this.expected = new HashSet<>();
        this.expected.add(IgnoredReplacement.of(1, "Enero"));
        this.expected.add(IgnoredReplacement.of(8, "Febrero"));
        this.expected.add(IgnoredReplacement.of(26, "Martes"));
    }

    @Test
    public void testUppercaseIndexOfFinder() {
        UppercaseIndexOfFinder finder = new UppercaseIndexOfFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testUppercaseRegexFinder() {
        UppercaseRegexFinder finder = new UppercaseRegexFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testUppercaseAutomatonFinder() {
        UppercaseAutomatonFinder finder = new UppercaseAutomatonFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testUppercaseRegexLookBehindFinder() {
        UppercaseRegexLookBehindFinder finder = new UppercaseRegexLookBehindFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testUppercaseAlternateRegexFinder() {
        UppercaseAlternateRegexFinder finder = new UppercaseAlternateRegexFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testUppercaseAlternateAutomatonFinder() {
        UppercaseAlternateAutomatonFinder finder = new UppercaseAlternateAutomatonFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testUppercaseAlternateRegexLookBehindFinder() {
        UppercaseAlternateRegexLookBehindFinder finder = new UppercaseAlternateRegexLookBehindFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

}
