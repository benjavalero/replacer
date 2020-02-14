package es.bvalero.replacer.finder.benchmark;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UppercaseFinderTest {
    private Collection<String> words;
    private String text;
    private Set<FinderResult> expected;

    @Before
    public void setUp() {
        this.words = Arrays.asList("Enero", "Febrero", "Lunes", "Martes");
        this.text = "=Enero. Febrero, Lunes #  Martes.";

        this.expected = new HashSet<>();
        this.expected.add(FinderResult.of(1, "Enero"));
        this.expected.add(FinderResult.of(8, "Febrero"));
        this.expected.add(FinderResult.of(26, "Martes"));
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