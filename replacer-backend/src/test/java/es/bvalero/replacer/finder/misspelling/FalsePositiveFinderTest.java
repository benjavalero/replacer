package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.finder.Immutable;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FalsePositiveFinderTest {
    private FalsePositiveFinder falsePositiveFinder;

    @Before
    public void setUp() {
        falsePositiveFinder = new FalsePositiveFinder();
    }

    @Test
    public void testRegexFalsePositives() {
        String text = "Un sólo de éstos en el Index Online.";
        Set<String> falsePositives = new HashSet<>(Arrays.asList("sólo", "ést?[aeo]s?", "Index", "online"));

        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(
            new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, falsePositives)
        );

        List<Immutable> matches = falsePositiveFinder.findList(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(4, matches.size());
        Assert.assertTrue(matches.contains(Immutable.of(3, "sólo", falsePositiveFinder)));
        Assert.assertTrue(matches.contains(Immutable.of(11, "éstos", falsePositiveFinder)));
        Assert.assertTrue(matches.contains(Immutable.of(23, "Index", falsePositiveFinder)));
        Assert.assertTrue(matches.contains(Immutable.of(29, "Online", falsePositiveFinder)));
    }

    @Test
    public void testNestedFalsePositives() {
        String text1 = "A Top Album Chart.";
        Set<String> falsePositives = new HashSet<>(Arrays.asList("Top Album", "Album Chart"));

        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(
            new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, falsePositives)
        );

        List<Immutable> matches1 = falsePositiveFinder.findList(text1);

        Assert.assertFalse(matches1.isEmpty());
        Assert.assertEquals(1, matches1.size());
        Assert.assertTrue(matches1.contains(Immutable.of(2, "Top Album", falsePositiveFinder)));

        // Only the first match is found
        Assert.assertFalse(matches1.contains(Immutable.of(6, "Album Chart", falsePositiveFinder)));

        String text2 = "A Topp Album Chart.";
        List<Immutable> matches2 = falsePositiveFinder.findList(text2);

        Assert.assertFalse(matches2.isEmpty());
        Assert.assertEquals(1, matches2.size());
        Assert.assertTrue(matches2.contains(Immutable.of(7, "Album Chart", falsePositiveFinder)));
    }

    @Test
    public void testFalsePositivesListEmpty() {
        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(
            new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, Collections.EMPTY_SET)
        );

        Assert.assertTrue(falsePositiveFinder.findList("A sample text").isEmpty());
    }

    @Test
    public void testFalsePositiveLowerCase() {
        String text = "A Test test.";
        Set<String> falsePositives = new HashSet<>(Collections.singletonList("test"));

        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(
            new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, falsePositives)
        );

        List<Immutable> matches = falsePositiveFinder.findList(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(2, matches.size());
        Assert.assertTrue(matches.contains(Immutable.of(2, "Test", falsePositiveFinder)));
        Assert.assertTrue(matches.contains(Immutable.of(7, "test", falsePositiveFinder)));
    }

    @Test
    public void testFalsePositiveUpperCase() {
        String text = "A Test test.";
        Set<String> falsePositives = new HashSet<>(Collections.singletonList("Test"));

        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(
            new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, falsePositives)
        );

        List<Immutable> matches = falsePositiveFinder.findList(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(1, matches.size());
        Assert.assertTrue(matches.contains(Immutable.of(2, "Test", falsePositiveFinder)));
    }

    @Test
    public void testFalsePositiveRegex() {
        String text = "A sample text.";
        Set<String> falsePositives = new HashSet<>(Collections.singletonList("(sample|text)"));

        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(
            new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, falsePositives)
        );

        List<Immutable> matches = falsePositiveFinder.findList(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(2, matches.size());
        Assert.assertTrue(matches.contains(Immutable.of(2, "sample", falsePositiveFinder)));
        Assert.assertTrue(matches.contains(Immutable.of(9, "text", falsePositiveFinder)));
    }
}
