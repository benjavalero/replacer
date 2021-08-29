package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
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

    private static final SetValuedMap<WikipediaLanguage, SimpleMisspelling> EMPTY_MAP = new HashSetValuedHashMap<>();

    private UppercaseAfterFinder uppercaseAfterFinder;

    @BeforeEach
    public void setUp() {
        uppercaseAfterFinder = new UppercaseAfterFinder();

        Set<String> uppercaseWords = Set.of("Enero", "Febrero", "Marzo", "Abril", "Mayo");
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> map = new HashSetValuedHashMap<>();
        for (String uppercaseWord : uppercaseWords) {
            map.put(
                WikipediaLanguage.getDefault(),
                SimpleMisspelling.of(uppercaseWord, true, uppercaseWord.toLowerCase())
            );
        }

        // Fake the update of the misspelling list in the misspelling manager
        uppercaseAfterFinder.propertyChange(
            new PropertyChangeEvent(this, SimpleMisspellingLoader.PROPERTY_ITEMS, EMPTY_MAP, map)
        );
    }

    @Test
    void testUppercaseAfterList() {
        String text = "\n" + "Enero\n" + "* Febrero\n" + "# Marzo\n" + "Abril";

        List<Immutable> matches = uppercaseAfterFinder.findList(text);

        Set<String> expected = Set.of("Febrero", "Marzo");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
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

    @Test
    void testUppercaseWordsFiltering() {
        SimpleMisspelling misspelling1 = SimpleMisspelling.of("Enero", true, "enero");
        SimpleMisspelling misspelling2 = SimpleMisspelling.of("Febrero", true, "febrero");
        SimpleMisspelling misspelling3 = SimpleMisspelling.of("habia", false, "había"); // Ignored
        SimpleMisspelling misspelling4 = SimpleMisspelling.of("madrid", true, "Madrid"); // Ignored
        SimpleMisspelling misspelling5 = SimpleMisspelling.of("Julio", true, "Julio, julio");
        SimpleMisspelling misspelling6 = SimpleMisspelling.of("Paris", true, "París"); // Ignored
        Set<SimpleMisspelling> misspellingSet = Set.of(
            misspelling1,
            misspelling2,
            misspelling3,
            misspelling4,
            misspelling5,
            misspelling6
        );
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.getDefault(), misspellingSet);

        Set<String> expectedWords = Set.of(misspelling1.getWord(), misspelling2.getWord(), misspelling5.getWord());
        Assertions.assertEquals(
            expectedWords,
            uppercaseAfterFinder.getUppercaseWords(map).get(WikipediaLanguage.getDefault())
        );
    }
}
