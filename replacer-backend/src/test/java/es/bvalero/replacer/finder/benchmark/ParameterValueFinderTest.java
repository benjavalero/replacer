package es.bvalero.replacer.finder.benchmark;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class ParameterValueFinderTest {

    private String text;
    private Set<IgnoredReplacement> expected;

    @Before
    public void setUp() {
        String value1 = "A\nvalue\n";
        String value2 = " Another value ";
        this.text = String.format("{{Template|index=%s| location =%s}}", value1, value2);

        this.expected = new HashSet<>();
        this.expected.add(IgnoredReplacement.of(17, value1));
        this.expected.add(IgnoredReplacement.of(37, value2));
    }

    @Test
    public void testParameterValueRegexFinder() {
        ParameterValueRegexFinder finder = new ParameterValueRegexFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testParameterValueRegexPossessiveFinder() {
        ParameterValueRegexPossessiveFinder finder = new ParameterValueRegexPossessiveFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testParameterValueRegexNoGroupFinder() {
        ParameterValueRegexNoGroupFinder finder = new ParameterValueRegexNoGroupFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testParameterValueAutomatonFinder() {
        ParameterValueAutomatonFinder finder = new ParameterValueAutomatonFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

}
