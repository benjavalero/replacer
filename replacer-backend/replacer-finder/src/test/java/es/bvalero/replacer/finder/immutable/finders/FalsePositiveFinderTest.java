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

    private void initFalsePositiveMap(String... falsePositives) {
        Set<FalsePositive> falsePositiveSet = Stream
            .of(falsePositives)
            .map(FalsePositive::of)
            .collect(Collectors.toSet());
        SetValuedMap<WikipediaLanguage, FalsePositive> falsePositiveMap = new HashSetValuedHashMap<>();
        falsePositiveMap.putAll(WikipediaLanguage.SPANISH, falsePositiveSet);
        falsePositiveFinder.propertyChange(
            new PropertyChangeEvent(this, FalsePositiveLoader.PROPERTY_ITEMS, EMPTY_MAP, falsePositiveMap)
        );
    }

    @Test
    void testFalsePositives() {
        String text = "Un sólo de éstos en el Index Online.";
        initFalsePositiveMap("[Ss]ólo", "[Éé]st?[aeo]s?", "Index", "[Oo]nline");

        List<Immutable> matches = falsePositiveFinder.findList(text);

        Set<String> expected = Set.of("sólo", "éstos", "Index", "Online");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void testFalsePositivesRepeated() {
        String text = "Para tí-tí había allí algo.";
        initFalsePositiveMap("tí", "había allí");

        List<Immutable> matches = falsePositiveFinder.findList(text);

        Set<String> expected = Set.of("tí", "había allí");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);

        assertEquals(2, matches.stream().filter(m -> m.getText().equals("tí")).count());
    }

    @Test
    void testFalsePositivesOverlappingComplete() {
        String text = "A Top Album Chart.";
        initFalsePositiveMap("Top Album", "Album Chart");

        List<Immutable> matches = falsePositiveFinder.findList(text);

        // Only the first match is found
        Set<String> expected = Set.of("Top Album");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void testFalsePositivesOverlappingNotComplete() {
        String text = "Los ratones aún son roedores.";
        initFalsePositiveMap("es aún", "aún son");

        List<Immutable> matches = falsePositiveFinder.findList(text);

        // This is a known issue.
        // In this case, the automaton finds the first match "es aún" and not the second one because they overlap.
        // So, once the first one is discarded as it is not complete, we lose the second one too.
        assertTrue(matches.isEmpty());
    }

    @Test
    void testFalsePositivesNotComplete() {
        String text = "A not_complete false positive.";
        initFalsePositiveMap("le", "not");

        List<Immutable> matches = falsePositiveFinder.findList(text);

        // No match should be returned as they are not complete
        assertTrue(matches.isEmpty());
    }

    @Test
    void testFalsePositivesListEmpty() {
        String text = "A sample text";
        initFalsePositiveMap();

        List<Immutable> matches = falsePositiveFinder.findList(text);

        // No match should be returned as they are not complete
        assertTrue(matches.isEmpty());
    }
}
