package es.bvalero.replacer.finder.benchmark.completetag;

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
    private Set<String> expected;

    @Before
    public void setUp() {
        this.words =
            Arrays.asList(
                "blockquote",
                "cite",
                "code",
                "math",
                "nowiki",
                "poem",
                "pre",
                "ref",
                "score",
                "source",
                "syntaxhighlight"
            );

        String tag1 = "<math class=\"latex\">Un <i>ejemplo</i>\n en LaTeX</math>";
        String tag2 = "<math>Otro ejemplo</math>";
        String tag3 = "<source>Otro ejemplo</source>";
        String tag4 = "<ref name=NH05/>";
        this.text = String.format("En %s %s %s %s.", tag1, tag2, tag3, tag4);

        this.expected = new HashSet<>(Arrays.asList(tag1, tag2, tag3));
    }

    @Test
    public void testCompleteTagRegexLazyFinder() {
        CompleteTagRegexLazyFinder finder = new CompleteTagRegexLazyFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCompleteTagRegexLazyNegatedFinder() {
        CompleteTagRegexNegatedLazyFinder finder = new CompleteTagRegexNegatedLazyFinder(words);
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
