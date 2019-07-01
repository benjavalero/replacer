package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class CursiveFinderTest {

    private String text;
    private Set<MatchResult> expected;

    @Before
    public void setUp() {
        String cursive1 = "''cursive1''";
        String cursive2 = "''cursive2''";
        String cursive3 = "''cursive3\n";
        String cursive4 = "''A '''''bold''''' inside''";
        String cursive5 = "''Beefeater's cool''"; // Only one letter after the inner quote not matched
        // We need more than one character between occurrences
        this.text = String.format("A %s - %s - %s - %s - %s.", cursive1, cursive2, cursive3, cursive4, cursive5);

        this.expected = new HashSet<>();
        this.expected.add(new MatchResult(2, cursive1));
        this.expected.add(new MatchResult(17, cursive2));
        this.expected.add(new MatchResult(32, cursive3));
        this.expected.add(new MatchResult(46, cursive4));
        this.expected.add(new MatchResult(76, cursive5));
    }

    @Test
    public void testCursiveRegexDotLazyFinder() {
        CursiveRegexDotLazyFinder finder = new CursiveRegexDotLazyFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCursiveRegexFinder() {
        CursiveRegexFinder finder = new CursiveRegexFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCursiveRegexDotAllLookFinder() {
        CursiveRegexDotAllLookFinder finder = new CursiveRegexDotAllLookFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCursiveRegexLookFinder() {
        CursiveRegexLookFinder finder = new CursiveRegexLookFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCursiveAutomatonFinder() {
        CursiveAutomatonFinder finder = new CursiveAutomatonFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

}
