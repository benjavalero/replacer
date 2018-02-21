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
        RegexMatch match = new RegexMatch(1, "X");

        Assert.assertEquals(0, match.compareTo(new RegexMatch(1, "X")));
        Assert.assertEquals(0, match.compareTo(new RegexMatch(1, "Z")));

        // The matches are sorted in descendant order of apparition
        Assert.assertTrue(match.compareTo(new RegexMatch(0, "Z")) < 0);
        Assert.assertTrue(match.compareTo(new RegexMatch(2, "Z")) > 0);
    }

    @Test
    public void testIsContainedIn() {
        RegexMatch match = new RegexMatch(5, "Cosas");

        Assert.assertFalse(new RegexMatch(0, "Casos").isContainedIn(match)); // Left-Out
        Assert.assertFalse(new RegexMatch(0, "Casita").isContainedIn(match)); // Left-Intersecting
        Assert.assertFalse(new RegexMatch(0, "Una casa muy bonita").isContainedIn(match)); // Containing
        Assert.assertTrue(new RegexMatch(5, "Casos").isContainedIn(match)); // Same interval
        Assert.assertTrue(new RegexMatch(6, "Red").isContainedIn(match)); // Contained
        Assert.assertFalse(new RegexMatch(9, "Casos").isContainedIn(match)); // Right-Intersecting
        Assert.assertFalse(new RegexMatch(10, "Casos").isContainedIn(match)); // Right-Out

        Assert.assertTrue(new RegexMatch(6, "Red").isContainedIn(
                Arrays.asList(new RegexMatch(0, "X"), match)));
    }

    @Test
    public void testRemoveNestedMatches() {
        RegexMatch match1 = new RegexMatch(1, "Hola"); // 1-5
        RegexMatch match2 = new RegexMatch(5, "Unas casas"); // 5-15
        RegexMatch match3 = new RegexMatch(10, "casa"); // 10-14 contained in match2
        RegexMatch match4 = new RegexMatch(20, "Hola"); // 20-24
        RegexMatch match5 = new RegexMatch(22, "lado"); // 22-26 intersects match4

        List<RegexMatch> matches = Arrays.asList(match2, match5, match4, match1, match3, match4);
        List<RegexMatch> resultMatches = RegexMatch.removedNestedMatches(matches);

        Assert.assertEquals(4, resultMatches.size());
        Assert.assertFalse(resultMatches.contains(match3));

        Assert.assertEquals(match5, resultMatches.get(0));
        Assert.assertEquals(match4, resultMatches.get(1));
        Assert.assertEquals(match2, resultMatches.get(2));
        Assert.assertEquals(match1, resultMatches.get(3));
    }

}
