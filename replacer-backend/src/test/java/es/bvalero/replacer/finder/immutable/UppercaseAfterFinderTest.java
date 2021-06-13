package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.MisspellingManager;
import java.beans.PropertyChangeEvent;
import java.util.Collections;
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

        SetValuedMap<WikipediaLanguage, String> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.getDefault(), Set.of("Enero", "Febrero", "Marzo", "Abril", "Mayo"));

        // Fake the update of the misspelling list in the misspelling manager
        uppercaseAfterFinder.propertyChange(
            new PropertyChangeEvent(this, MisspellingManager.PROPERTY_UPPERCASE_WORDS, EMPTY_MAP, map)
        );
    }

    @Test
    void testUppercaseAfterList() {
        String text = "\n" + "Enero\n" + "* Febrero\n" + "# Marzo\n" + "Abril";

        List<Immutable> matches = uppercaseAfterFinder.findList(text);

        Set<String> expected = Set.of("Febrero", "Marzo");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);

        // Check positions
        Assertions.assertTrue(
            matches.stream().allMatch(m -> text.substring(m.getStart(), m.getEnd()).equals(m.getText()))
        );
    }

    @Test
    void testUppercaseAfterDot() {
        String text = "En Enero. Febrero.";

        List<Immutable> matches = uppercaseAfterFinder.findList(text);

        Set<String> expected = Set.of("Febrero");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testUppercaseAfterParameter() {
        String text = "{{ param=Enero }}";

        List<Immutable> matches = uppercaseAfterFinder.findList(text);

        Set<String> expected = Set.of("Enero");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testUppercaseAfterTableCell() {
        String text = "{|\n" + "|+ Marzo\n" + "|-\n" + "! Abril !! Mayo\n" + "|-\n" + "|Enero||Febrero\n" + "|}";

        List<Immutable> matches = uppercaseAfterFinder.findList(text);

        Set<String> expected = Set.of("Enero", "Febrero", "Marzo", "Abril", "Mayo");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testUppercaseAfterLinkAlias() {
        String text = "En [[Enero|Enero]]";

        List<Immutable> matches = uppercaseAfterFinder.findList(text);

        Set<String> expected = Collections.emptySet();
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testUppercaseAfterHtmlTableCell() {
        String text = "<table><tr><td>Marzo</td></tr></table>";

        List<Immutable> matches = uppercaseAfterFinder.findList(text);

        Set<String> expected = Set.of("Marzo");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }
}
