package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.finder.MatchResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.util.*;

public class FalsePositiveFinderTest {

    private FalsePositiveFinder falsePositiveFinder;

    @Before
    public void setUp() {
        falsePositiveFinder = new FalsePositiveFinder();
    }

    @Test
    public void testRegexFalsePositives() {
        String text = "Un sólo de éstos en el Index Online.";
        Set<String> falsePositives = new HashSet<>(Arrays.asList("[Ss]ólo", "[Éé]st?[aeo]s?", "Index", "[Oo]nline"));

        // Fake the update of the misspelling list in the misspelling manager
        falsePositiveFinder.propertyChange(new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, falsePositives));

        List<MatchResult> matches = falsePositiveFinder.findIgnoredReplacements(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(4, matches.size());
        Assert.assertTrue(matches.contains(new MatchResult(3, "sólo")));
        Assert.assertTrue(matches.contains(new MatchResult(11, "éstos")));
        Assert.assertTrue(matches.contains(new MatchResult(23, "Index")));
        Assert.assertTrue(matches.contains(new MatchResult(29, "Online")));
    }

    @Test
    public void testNestedFalsePositives() {
        String text1 = "A Top Album Chart.";
        Set<String> falsePositives = new HashSet<>(Arrays.asList("Top Album", "Album Chart"));

        // Fake the update of the misspelling list in the misspelling manager
        falsePositiveFinder.propertyChange(new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, falsePositives));

        List<MatchResult> matches1 = falsePositiveFinder.findIgnoredReplacements(text1);

        Assert.assertFalse(matches1.isEmpty());
        Assert.assertEquals(1, matches1.size());
        Assert.assertTrue(matches1.contains(new MatchResult(2, "Top Album")));
        // Only the first match is found
        Assert.assertFalse(matches1.contains(new MatchResult(6, "Album Chart")));

        String text2 = "A Topp Album Chart.";
        List<MatchResult> matches2 = falsePositiveFinder.findIgnoredReplacements(text2);

        Assert.assertFalse(matches2.isEmpty());
        Assert.assertEquals(1, matches2.size());
        Assert.assertTrue(matches2.contains(new MatchResult(7, "Album Chart")));
    }

}
