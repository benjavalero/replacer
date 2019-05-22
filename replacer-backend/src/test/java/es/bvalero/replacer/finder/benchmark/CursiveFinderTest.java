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
        String cursive1 = "''A cursive text''";
        String cursive2 = "''A cursive text not finished\n";
        String cursive3 = "''A cursive text with '''''bold''''' inside''";
        this.text = String.format("%s %s %s", cursive1, cursive2, cursive3);

        this.expected = new HashSet<>();
        this.expected.add(new MatchResult(0, cursive1));
        this.expected.add(new MatchResult(19, cursive2));
        this.expected.add(new MatchResult(50, cursive3));
    }

    @Test
    public void testCursiveRegexFinder() {
        CursiveRegexFinder finder = new CursiveRegexFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCursiveRegexPossessiveFinder() {
        CursiveRegexPossessiveFinder finder = new CursiveRegexPossessiveFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCursiveAutomatonFinder() {
        CursiveAutomatonFinder finder = new CursiveAutomatonFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

}
