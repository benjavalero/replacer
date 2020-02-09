package es.bvalero.replacer.finder.benchmark;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CompleteTagFinderTest {
    private List<String> words;
    private String text;
    private Set<IgnoredReplacement> expected;

    @Before
    public void setUp() {
        this.words = Arrays.asList("pre", "poem", "source");

        String tag1 = "<pre>Text with <span>tag</span> inside.</pre>";
        String tag2 = "<poem class>\nA line\nAnother line\n</poem>";
        String tag3 = "<source>Otro ejemplo</source>";

        this.text = String.format("%s %s %s", tag1, tag2, tag3);

        this.expected = new HashSet<>();
        this.expected.add(IgnoredReplacement.of(0, tag1));
        this.expected.add(IgnoredReplacement.of(46, tag2));
        this.expected.add(IgnoredReplacement.of(87, tag3));
    }

    @Test
    public void testCompleteTagRegexFinder() {
        CompleteTagRegexFinder finder = new CompleteTagRegexFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCompleteTagRegexLazyLazyFinder() {
        CompleteTagRegexLazyFinder finder = new CompleteTagRegexLazyFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCompleteTagRegexNegatedFinder() {
        CompleteTagRegexNegatedFinder finder = new CompleteTagRegexNegatedFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCompleteTagRegexLazyLazyNegatedFinder() {
        CompleteTagRegexNegatedLazyFinder finder = new CompleteTagRegexNegatedLazyFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCompleteTagAutomatonFinder() {
        CompleteTagAutomatonFinder finder = new CompleteTagAutomatonFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCompleteTagAutomatonNegatedFinder() {
        CompleteTagAutomatonNegatedFinder finder = new CompleteTagAutomatonNegatedFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCompleteTagRegexAlternateFinder() {
        CompleteTagRegexAlternateFinder finder = new CompleteTagRegexAlternateFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCompleteTagRegexAlternateNegatedFinder() {
        CompleteTagRegexAlternateNegatedFinder finder = new CompleteTagRegexAlternateNegatedFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }
}
