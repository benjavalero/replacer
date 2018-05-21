package es.bvalero.replacer.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class RegexMatchTest {

    @Test
    public void testEquals() {
        RegexMatch match = new RegexMatch(1, "X");

        Assert.assertEquals(match, match);
        Assert.assertEquals(match, new RegexMatch(1, "X"));
        Assert.assertNotEquals(match, new RegexMatch(1, "Z"));
        Assert.assertNotEquals(match, new RegexMatch(0, "X"));
        Assert.assertNotEquals(match, new RegexMatch(0, "Z"));
    }

    @Test
    public void testCompare() {
        RegexMatch match = new RegexMatch(1, "XXX");

        Assert.assertEquals(0, match.compareTo(new RegexMatch(1, "XXX")));
        Assert.assertEquals(0, match.compareTo(new RegexMatch(1, "ZZZ")));

        // The matches are sorted in descendant order of apparition
        Assert.assertTrue(match.compareTo(new RegexMatch(0, "Z")) < 0);
        Assert.assertTrue(match.compareTo(new RegexMatch(2, "Z")) > 0);
        Assert.assertTrue(match.compareTo(new RegexMatch(1, "ZZ")) > 0);
    }

    @Test
    public void testRemoveNestedMatches() {
        // Sample text: F R E N É T I C A M E N T E
        RegexMatch match1 = new RegexMatch(1, "REN"); // 1-3
        RegexMatch match2 = new RegexMatch(4, "ÉTICA"); // 4-8
        RegexMatch match3 = new RegexMatch(7, "CAMEN"); // 7-11
        RegexMatch match4 = new RegexMatch(12, "TE"); // 12-13
        RegexMatch match5 = new RegexMatch(9, "MEN"); // 9-11
        RegexMatch match6 = new RegexMatch(4, "ET"); // 4-5
        RegexMatch match7 = new RegexMatch(7, "CAM"); // 7-9

        List<RegexMatch> matches = Arrays.asList(match2, match5, match4, match1, match3, match4, match6, match7);
        List<RegexMatch> resultMatches = RegexMatch.removedNestedMatches(matches);

        Assert.assertEquals(3, resultMatches.size());
        Assert.assertEquals(match4, resultMatches.get(0));
        Assert.assertEquals(match2, resultMatches.get(1));
        Assert.assertEquals("ÉTICAMEN", resultMatches.get(1).getOriginalText());
        Assert.assertEquals(match1, resultMatches.get(2));
    }

}
