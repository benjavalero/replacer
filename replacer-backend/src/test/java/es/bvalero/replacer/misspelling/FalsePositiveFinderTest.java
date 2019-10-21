package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.finder.IgnoredReplacement;
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

        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, falsePositives));

        List<IgnoredReplacement> matches = falsePositiveFinder.findIgnoredReplacements(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(4, matches.size());
        Assert.assertTrue(matches.contains(IgnoredReplacement.of(3, "sólo")));
        Assert.assertTrue(matches.contains(IgnoredReplacement.of(11, "éstos")));
        Assert.assertTrue(matches.contains(IgnoredReplacement.of(23, "Index")));
        Assert.assertTrue(matches.contains(IgnoredReplacement.of(29, "Online")));
    }

    @Test
    public void testNestedFalsePositives() {
        String text1 = "A Top Album Chart.";
        Set<String> falsePositives = new HashSet<>(Arrays.asList("Top Album", "Album Chart"));

        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, falsePositives));

        List<IgnoredReplacement> matches1 = falsePositiveFinder.findIgnoredReplacements(text1);

        Assert.assertFalse(matches1.isEmpty());
        Assert.assertEquals(1, matches1.size());
        Assert.assertTrue(matches1.contains(IgnoredReplacement.of(2, "Top Album")));
        // Only the first match is found
        Assert.assertFalse(matches1.contains(IgnoredReplacement.of(6, "Album Chart")));

        String text2 = "A Topp Album Chart.";
        List<IgnoredReplacement> matches2 = falsePositiveFinder.findIgnoredReplacements(text2);

        Assert.assertFalse(matches2.isEmpty());
        Assert.assertEquals(1, matches2.size());
        Assert.assertTrue(matches2.contains(IgnoredReplacement.of(7, "Album Chart")));
    }

    @Test
    public void testFalsePositivesListEmpty() {
        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, Collections.EMPTY_SET));

        Assert.assertTrue(falsePositiveFinder.findIgnoredReplacements("A sample text").isEmpty());
    }

}
