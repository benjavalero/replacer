package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.listing.ComposedMisspelling;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.StandardMisspelling;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

class UppercaseFinderTest {

    private static final SetValuedMap<WikipediaLanguage, SimpleMisspelling> EMPTY_MAP = new HashSetValuedHashMap<>();

    @Mock
    private SimpleMisspellingLoader simpleMisspellingLoader;

    @Mock
    private ComposedMisspellingLoader composedMisspellingLoader;

    private UppercaseFinder uppercaseFinder;

    @BeforeEach
    public void setUp() {
        uppercaseFinder = new UppercaseFinder(simpleMisspellingLoader, composedMisspellingLoader);
        initSimpleUppercaseMap("Enero", "Febrero", "Marzo", "Abril", "Mayo");
        initComposedUppercaseMap("Jefe de Estado");
    }

    private void initSimpleUppercaseMap(String... uppercaseWords) {
        Set<SimpleMisspelling> uppercaseSet = Stream.of(uppercaseWords)
            .map(uppercaseWord ->
                SimpleMisspelling.of(uppercaseWord, true, FinderUtils.setFirstLowerCase(uppercaseWord))
            )
            .collect(Collectors.toSet());
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> uppercaseMap = new HashSetValuedHashMap<>();
        uppercaseMap.putAll(WikipediaLanguage.SPANISH, uppercaseSet);
        uppercaseFinder.propertyChange(
            new PropertyChangeEvent(this, SimpleMisspellingLoader.LABEL_SIMPLE_MISSPELLING, EMPTY_MAP, uppercaseMap)
        );
    }

    private void initComposedUppercaseMap(String... uppercaseWords) {
        Set<ComposedMisspelling> uppercaseSet = Stream.of(uppercaseWords)
            .map(uppercaseWord ->
                ComposedMisspelling.of(uppercaseWord, true, FinderUtils.setFirstLowerCase(uppercaseWord))
            )
            .collect(Collectors.toSet());
        SetValuedMap<WikipediaLanguage, ComposedMisspelling> uppercaseMap = new HashSetValuedHashMap<>();
        uppercaseMap.putAll(WikipediaLanguage.SPANISH, uppercaseSet);
        uppercaseFinder.propertyChange(
            new PropertyChangeEvent(this, ComposedMisspellingLoader.LABEL_COMPOSED_MISSPELLING, EMPTY_MAP, uppercaseMap)
        );
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "Enero\n* Febrero\nMarzo", // Unordered list
            "Enero\n# Febrero\nMarzo", // Ordered list
            "Enero. Febrero.", // After dot
            "{{ param=Febrero }}", // Parameter value
            "{{ param = Febrero }}", // Parameter value
            "{{param=[[Febrero]] de [[1980]]}}", // Parameter value with link
            """
            {|
            |+ Febrero
            |}""", // Table row properties
            """
            {|
            |-
            ! Febrero !! Junio
            |}""", // Table header
            """
            {|
            |-
            ! Junio !! Febrero
            |}""", // Table header
            """
            {|
            |-
            | Febrero || Junio
            |}""", // Table cell
            """
            {|
            |-
            | Junio || Febrero
            |}""", // Table cell
            """
            {|
            |-
            | align="center" | Febrero
            |}""", // Table cell with style
            """
            {|
            |-
            | [[Febrero]] || Texto
            |}""", // Table cell with link
            "<table><tr><td>Febrero</td></tr></table>", // HTML cell
            "=== Febrero ===", // Header
            "=== [[Febrero]] ===", // Header with link
            "</ref> Febrero y tal", // After reference
            "En <small>[[Febrero]]</small>.", // Inside HTML tags
            "\n\nFebrero y marzo", // Start of paragraph
        }
    )
    void testUppercase(String text) {
        List<Immutable> matches = uppercaseFinder.findList(text);

        Set<String> expected = Set.of("Febrero");
        Set<String> actual = matches.stream().map(Immutable::text).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void testUppercaseLinkAlias() {
        String text = "En [[Enero|Enero]]";

        List<Immutable> matches = uppercaseFinder.findList(text);

        // Uppercase preceded by link pipes are not taken into account
        assertTrue(matches.isEmpty());
    }

    @Test
    void testUppercaseComposedValue() {
        String text = "{{T|param = Jefe de Estado }}";

        List<Immutable> matches = uppercaseFinder.findList(text);

        Set<String> expected = Set.of("Jefe de Estado");
        Set<String> actual = matches.stream().map(Immutable::text).collect(Collectors.toSet());
        assertEquals(expected, actual);
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
        SetValuedMap<WikipediaLanguage, StandardMisspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.getDefault(), misspellingSet);

        Set<String> expectedWords = Set.of(misspelling1.getWord(), misspelling2.getWord(), misspelling5.getWord());
        assertEquals(expectedWords, uppercaseFinder.getUppercaseWords(map).get(WikipediaLanguage.getDefault()));
    }
}
