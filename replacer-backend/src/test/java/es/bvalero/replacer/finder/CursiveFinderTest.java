package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CursiveFinderTest {

    @Test
    public void testCursiveFinder() {
        String cursive1 = "''A cursive text''";
        String cursive2 = "''A cursive text not finished\n";
        String cursive3 = "''A cursive text with '''''bold''''' inside''";
        String text = String.format("%s %s %s", cursive1, cursive2, cursive3);

        IgnoredReplacementFinder quotesFinder = new CursiveFinder();

        List<MatchResult> matches = quotesFinder.findIgnoredReplacements(text);

        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(cursive1, matches.get(0).getText());
        Assert.assertEquals(cursive2, matches.get(1).getText());
        Assert.assertEquals(cursive3, matches.get(2).getText());
    }

}
