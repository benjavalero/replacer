package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.beans.PropertyChangeEvent;
import java.util.*;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FalsePositiveFinderTest {
    private static final SetValuedMap<WikipediaLanguage, String> EMPTY_MAP = new HashSetValuedHashMap<>();

    private FalsePositiveFinder falsePositiveFinder;

    @BeforeEach
    public void setUp() {
        falsePositiveFinder = new FalsePositiveFinder();
    }

    @Test
    public void testRegexFalsePositives() {
        String text = "Un sólo de éstos en el Index Online.";
        Set<String> falsePositives = new HashSet<>(Arrays.asList("sólo", "ést?[aeo]s?", "Index", "online"));
        SetValuedMap<WikipediaLanguage, String> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, falsePositives);

        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Immutable> matches = falsePositiveFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertFalse(matches.isEmpty());
        Assertions.assertEquals(4, matches.size());
        Assertions.assertTrue(matches.contains(Immutable.of(3, "sólo", falsePositiveFinder)));
        Assertions.assertTrue(matches.contains(Immutable.of(11, "éstos", falsePositiveFinder)));
        Assertions.assertTrue(matches.contains(Immutable.of(23, "Index", falsePositiveFinder)));
        Assertions.assertTrue(matches.contains(Immutable.of(29, "Online", falsePositiveFinder)));
    }

    @Test
    public void testNestedFalsePositives() {
        String text1 = "A Top Album Chart.";
        Set<String> falsePositives = new HashSet<>(Arrays.asList("Top Album", "Album Chart"));
        SetValuedMap<WikipediaLanguage, String> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, falsePositives);

        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Immutable> matches1 = falsePositiveFinder.findList(text1, WikipediaLanguage.SPANISH);

        Assertions.assertFalse(matches1.isEmpty());
        Assertions.assertEquals(1, matches1.size());
        Assertions.assertTrue(matches1.contains(Immutable.of(2, "Top Album", falsePositiveFinder)));

        // Only the first match is found
        Assertions.assertFalse(matches1.contains(Immutable.of(6, "Album Chart", falsePositiveFinder)));

        String text2 = "A Topp Album Chart.";
        List<Immutable> matches2 = falsePositiveFinder.findList(text2, WikipediaLanguage.SPANISH);

        Assertions.assertFalse(matches2.isEmpty());
        Assertions.assertEquals(1, matches2.size());
        Assertions.assertTrue(matches2.contains(Immutable.of(7, "Album Chart", falsePositiveFinder)));
    }

    @Test
    public void testFalsePositivesListEmpty() {
        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, EMPTY_MAP));

        Assertions.assertTrue(falsePositiveFinder.findList("A sample text").isEmpty());
    }

    @Test
    public void testFalsePositiveLowerCase() {
        String text = "A Test test.";
        Set<String> falsePositives = new HashSet<>(Collections.singletonList("test"));
        SetValuedMap<WikipediaLanguage, String> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, falsePositives);

        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Immutable> matches = falsePositiveFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertFalse(matches.isEmpty());
        Assertions.assertEquals(2, matches.size());
        Assertions.assertTrue(matches.contains(Immutable.of(2, "Test", falsePositiveFinder)));
        Assertions.assertTrue(matches.contains(Immutable.of(7, "test", falsePositiveFinder)));
    }

    @Test
    public void testFalsePositiveUpperCase() {
        String text = "A Test test.";
        Set<String> falsePositives = new HashSet<>(Collections.singletonList("Test"));
        SetValuedMap<WikipediaLanguage, String> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, falsePositives);

        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Immutable> matches = falsePositiveFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertFalse(matches.isEmpty());
        Assertions.assertEquals(1, matches.size());
        Assertions.assertTrue(matches.contains(Immutable.of(2, "Test", falsePositiveFinder)));
    }

    @Test
    public void testFalsePositiveRegex() {
        String text = "A sample text.";
        Set<String> falsePositives = new HashSet<>(Collections.singletonList("(sample|text)"));
        SetValuedMap<WikipediaLanguage, String> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, falsePositives);

        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(new PropertyChangeEvent(this, "name", EMPTY_MAP, map));

        List<Immutable> matches = falsePositiveFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertFalse(matches.isEmpty());
        Assertions.assertEquals(2, matches.size());
        Assertions.assertTrue(matches.contains(Immutable.of(2, "sample", falsePositiveFinder)));
        Assertions.assertTrue(matches.contains(Immutable.of(9, "text", falsePositiveFinder)));
    }
}
