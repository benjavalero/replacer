package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.listing.FalsePositive;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FalsePositiveFinderTest {

    private static final SetValuedMap<WikipediaLanguage, String> EMPTY_MAP = new HashSetValuedHashMap<>();

    private FalsePositiveFinder falsePositiveFinder;

    @BeforeEach
    public void setUp() {
        falsePositiveFinder = new FalsePositiveFinder();
    }

    @Test
    void testRegexFalsePositives() {
        String text = "Un sólo de éstos en el Index Online.";
        Set<FalsePositive> falsePositives = Stream
            .of("[Ss]ólo", "[Éé]st?[aeo]s?", "Index", "[Oo]nline")
            .map(FalsePositive::of)
            .collect(Collectors.toSet());
        SetValuedMap<WikipediaLanguage, FalsePositive> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, falsePositives);

        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(
            new PropertyChangeEvent(this, FalsePositiveLoader.PROPERTY_ITEMS, EMPTY_MAP, map)
        );

        List<Immutable> matches = falsePositiveFinder.findList(text);

        Assertions.assertFalse(matches.isEmpty());
        Assertions.assertEquals(4, matches.size());
        Assertions.assertTrue(matches.contains(Immutable.of(3, "sólo")));
        Assertions.assertTrue(matches.contains(Immutable.of(11, "éstos")));
        Assertions.assertTrue(matches.contains(Immutable.of(23, "Index")));
        Assertions.assertTrue(matches.contains(Immutable.of(29, "Online")));
    }

    @Test
    void testNestedFalsePositives() {
        String text1 = "A Top Album Chart.";
        Set<FalsePositive> falsePositives = Stream
            .of("Top Album", "Album Chart")
            .map(FalsePositive::of)
            .collect(Collectors.toSet());
        SetValuedMap<WikipediaLanguage, FalsePositive> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, falsePositives);

        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(
            new PropertyChangeEvent(this, FalsePositiveLoader.PROPERTY_ITEMS, EMPTY_MAP, map)
        );

        List<Immutable> matches1 = falsePositiveFinder.findList(text1);

        Assertions.assertFalse(matches1.isEmpty());
        Assertions.assertEquals(1, matches1.size());
        Assertions.assertTrue(matches1.contains(Immutable.of(2, "Top Album")));

        // Only the first match is found
        Assertions.assertFalse(matches1.contains(Immutable.of(6, "Album Chart")));

        String text2 = "A Topp Album Chart.";
        List<Immutable> matches2 = falsePositiveFinder.findList(text2);

        Assertions.assertFalse(matches2.isEmpty());
        Assertions.assertEquals(1, matches2.size());
        Assertions.assertTrue(matches2.contains(Immutable.of(7, "Album Chart")));
    }

    @Test
    void testFalsePositivesListEmpty() {
        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(
            new PropertyChangeEvent(this, FalsePositiveLoader.PROPERTY_ITEMS, EMPTY_MAP, EMPTY_MAP)
        );

        Assertions.assertTrue(falsePositiveFinder.findList("A sample text").isEmpty());
    }
}