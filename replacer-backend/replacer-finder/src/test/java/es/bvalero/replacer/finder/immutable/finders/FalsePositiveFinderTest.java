package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.listing.FalsePositive;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
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

        assertFalse(matches.isEmpty());
        assertEquals(4, matches.size());
        assertTrue(matches.contains(Immutable.of(3, "sólo")));
        assertTrue(matches.contains(Immutable.of(11, "éstos")));
        assertTrue(matches.contains(Immutable.of(23, "Index")));
        assertTrue(matches.contains(Immutable.of(29, "Online")));
    }

    @Test
    void testRepeatedFalsePositives() {
        String text = "Para tí-tí había allí algo.";
        Set<FalsePositive> falsePositives = Stream
            .of("tí", "había allí")
            .map(FalsePositive::of)
            .collect(Collectors.toSet());
        SetValuedMap<WikipediaLanguage, FalsePositive> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, falsePositives);

        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(
            new PropertyChangeEvent(this, FalsePositiveLoader.PROPERTY_ITEMS, EMPTY_MAP, map)
        );

        List<Immutable> matches = falsePositiveFinder.findList(text);

        assertFalse(matches.isEmpty());
        assertEquals(3, matches.size());
        assertTrue(matches.contains(Immutable.of(5, "tí")));
        assertTrue(matches.contains(Immutable.of(8, "tí")));
        assertTrue(matches.contains(Immutable.of(11, "había allí")));
    }

    @Test
    void testOverlappingCompleteFalsePositives() {
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

        assertFalse(matches1.isEmpty());
        assertEquals(1, matches1.size());
        assertTrue(matches1.contains(Immutable.of(2, "Top Album")));

        // Only the first match is found
        assertFalse(matches1.contains(Immutable.of(6, "Album Chart")));
    }

    @Test
    void testOverlappingNotCompleteFalsePositives() {
        String text1 = "Los ratones aún son roedores.";
        Set<FalsePositive> falsePositives = Stream
            .of("es aún", "aún son")
            .map(FalsePositive::of)
            .collect(Collectors.toSet());
        SetValuedMap<WikipediaLanguage, FalsePositive> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, falsePositives);

        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(
            new PropertyChangeEvent(this, FalsePositiveLoader.PROPERTY_ITEMS, EMPTY_MAP, map)
        );

        List<Immutable> matches = falsePositiveFinder.findList(text1);

        // This is a known issue.
        // In this case, the automaton finds the first match "es aún" and not the second one because they overlap.
        // So, once the first one is discarded as it is not complete, we lose the second one too.
        assertTrue(matches.isEmpty());
    }

    @Test
    void testNotCompleteFalsePositives() {
        String text1 = "A not_complete false positive.";
        Set<FalsePositive> falsePositives = Stream.of("le", "not").map(FalsePositive::of).collect(Collectors.toSet());
        SetValuedMap<WikipediaLanguage, FalsePositive> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, falsePositives);

        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(
            new PropertyChangeEvent(this, FalsePositiveLoader.PROPERTY_ITEMS, EMPTY_MAP, map)
        );

        List<Immutable> matches = falsePositiveFinder.findList(text1);

        // No match should be returned as they are not complete
        assertTrue(matches.isEmpty());
    }

    @Test
    void testFalsePositivesListEmpty() {
        // Fake the update of the list in the manager
        falsePositiveFinder.propertyChange(
            new PropertyChangeEvent(this, FalsePositiveLoader.PROPERTY_ITEMS, EMPTY_MAP, EMPTY_MAP)
        );

        assertTrue(falsePositiveFinder.findList("A sample text").isEmpty());
    }
}
