package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MatchResultTest {

    @Test
    public void testCompare() {
        MatchResult result1 = new MatchResult(0, "A");
        MatchResult result2 = new MatchResult(0, "AB");
        MatchResult result3 = new MatchResult(1, "BC");
        MatchResult result4 = new MatchResult(1, "BCD");
        MatchResult result5 = new MatchResult(2, "C");
        List<MatchResult> results = Arrays.asList(result1, result2, result3, result4, result5);

        Collections.sort(results);

        // Order descendant by start. If equals, the lower end.
        Assert.assertEquals(result5, results.get(0));
        Assert.assertEquals(result3, results.get(1));
        Assert.assertEquals(result4, results.get(2));
        Assert.assertEquals(result1, results.get(3));
        Assert.assertEquals(result2, results.get(4));
    }

    @Test
    public void testIsContained() {
        MatchResult result1 = new MatchResult(0, "A");
        MatchResult result2 = new MatchResult(1, "BC");
        List<MatchResult> results = Arrays.asList(result1, result2);

        Assert.assertTrue(result1.isContainedIn(results));
        Assert.assertTrue(result2.isContainedIn(results));
        MatchResult result3 = new MatchResult(1, "B");
        Assert.assertTrue(result3.isContainedIn(results));
        MatchResult result4 = new MatchResult(0, "AB");
        Assert.assertFalse(result4.isContainedIn(results));
        MatchResult result5 = new MatchResult(0, "ABC");
        Assert.assertFalse(result5.isContainedIn(results));
        MatchResult result6 = new MatchResult(2, "C");
        Assert.assertTrue(result6.isContainedIn(results));
    }

}
