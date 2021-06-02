package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.MisspellingManager;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UppercaseAfterFinderTest {

    private static final SetValuedMap<WikipediaLanguage, Misspelling> EMPTY_MAP = new HashSetValuedHashMap<>();

    private UppercaseAfterFinder uppercaseAfterFinder;

    @BeforeEach
    public void setUp() {
        uppercaseAfterFinder = new UppercaseAfterFinder();
    }

    @Test
    void testRegexUppercaseAfter() {
        String noun1 = "Enero";
        String noun2 = "Febrero";
        String text = "{{ param=" + noun1 + " | " + noun2 + " }} zzz";

        SetValuedMap<WikipediaLanguage, String> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.getDefault(), Set.of("Enero", "Febrero"));

        // Fake the update of the misspelling list in the misspelling manager
        uppercaseAfterFinder.propertyChange(
            new PropertyChangeEvent(this, MisspellingManager.PROPERTY_UPPERCASE_WORDS, EMPTY_MAP, map)
        );

        List<Immutable> matches = uppercaseAfterFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(noun1, noun2));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }
}
