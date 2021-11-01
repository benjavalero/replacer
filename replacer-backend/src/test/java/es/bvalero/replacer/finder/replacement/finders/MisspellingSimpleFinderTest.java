package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementSuggestion;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MisspellingSimpleFinderTest {

    private SimpleMisspellingLoader simpleMisspellingLoader;
    private MisspellingSimpleFinder misspellingFinder;

    @BeforeEach
    void setUp() {
        simpleMisspellingLoader = new SimpleMisspellingLoader();
        misspellingFinder = new MisspellingSimpleFinder();
        misspellingFinder.setSimpleMisspellingLoader(simpleMisspellingLoader);
    }

    private void fakeUpdateMisspellingList(List<SimpleMisspelling> misspellings) {
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellings);

        simpleMisspellingLoader.setItems(map);
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> emptyMap = new HashSetValuedHashMap<>();
        misspellingFinder.propertyChange(
            new PropertyChangeEvent(this, SimpleMisspellingLoader.PROPERTY_ITEMS, emptyMap, map)
        );
    }

    @Test
    void testNoResults() {
        String text = "Un texto";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("abadia", "abadía");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        assertTrue(results.isEmpty());
    }

    @Test
    void testOneResult() {
        String text = "Una abadia.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("abadia", "abadía");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(4)
            .text("abadia")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("abadia")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("abadía")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testTwoResults() {
        String text = "Una abadia online.";

        SimpleMisspelling misspelling1 = SimpleMisspelling.ofCaseInsensitive("abadia", "abadía");
        SimpleMisspelling misspelling2 = SimpleMisspelling.ofCaseInsensitive("online", "en línea");
        this.fakeUpdateMisspellingList(List.of(misspelling1, misspelling2));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected1 = Replacement
            .builder()
            .start(4)
            .text("abadia")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("abadia")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("abadía")))
            .build();
        Replacement expected2 = Replacement
            .builder()
            .start(11)
            .text("online")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("online")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("en línea")))
            .build();
        assertEquals(Set.of(expected1, expected2), new HashSet<>(results));
    }

    @Test
    void testUppercase() {
        String text = "Una Abadia.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("abadia", "abadía");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(4)
            .text("Abadia")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("abadia")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("Abadía")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveUppercaseVersusLowercase() {
        String text = "En enero.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseSensitive("Enero", "enero");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        assertEquals(Collections.emptySet(), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveUppercase() {
        String text = "En Brazil.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseSensitive("Brazil", "Brasil");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("Brazil")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("Brazil")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("Brasil")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveUppercaseToLowercase() {
        String text = "En Enero.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseSensitive("Enero", "enero");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        ReplacementSuggestion suggestion = ReplacementSuggestion.ofNoComment("enero");
        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("Enero")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("Enero")
            .suggestions(List.of(suggestion.toUppercase(), suggestion))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveLowercaseVersusUppercase() {
        String text = "En Angola.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseSensitive("angola", "Angola");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        assertEquals(Collections.emptySet(), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveLowercase() {
        String text = "En ves.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseSensitive("ves", "vez");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("ves")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("ves")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("vez")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveLowercaseToUppercase() {
        String text = "En angola.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseSensitive("angola", "Angola");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("angola")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("angola")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("Angola")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testCompleteWord() {
        String text = "Un texto";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("text", "texto");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        assertTrue(results.isEmpty());
    }

    @Test
    void testAllUppercaseCaseInsensitive() {
        String text = "Una ABADIA.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("abadia", "abadía");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        assertTrue(results.isEmpty());
    }

    @Test
    void testAllUppercaseCaseSensitive() {
        String text = "Un OVNI.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseSensitive("OVNI", "ovni");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("OVNI")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("OVNI")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("ovni")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testBetweenUnderscores() {
        String text = "A _Text Text_ _Text_ Text.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("text", "texto");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(21)
            .text("Text")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("text")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("Texto")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testSameWordFirst() {
        String text = "Un entreno.";

        String word = "entreno";
        String comment = "entrenó (verbo), entreno (sustantivo)";
        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive(word, comment);
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("entreno")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("entreno")
            .suggestions(
                List.of(ReplacementSuggestion.of("entreno", "sustantivo"), ReplacementSuggestion.of("entrenó", "verbo"))
            )
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testMisspellingWithDot() {
        assertThrows(IllegalArgumentException.class, () -> SimpleMisspelling.ofCaseInsensitive("cms.", "cm"));
    }

    @Test
    void testMisspellingWithNumber() {
        assertThrows(IllegalArgumentException.class, () -> SimpleMisspelling.ofCaseInsensitive("m2", "m²"));
    }

    @Test
    void testSuggestionsDifferentCaseLowercase() {
        String text = "Un avion.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("avion", "avión (aeronave), Avión (río)");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("avion")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("avion")
            .suggestions(
                List.of(ReplacementSuggestion.of("avión", "aeronave"), ReplacementSuggestion.of("Avión", "río"))
            )
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testSuggestionsDifferentCaseUppercase() {
        String text = "Un Avion.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("avion", "avión (aeronave), Avión (río)");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("Avion")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("avion")
            .suggestions(
                List.of(ReplacementSuggestion.of("Avión", "aeronave"), ReplacementSuggestion.of("Avión", "río"))
            )
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testLowercaseCaseSensitiveSuggestionsDifferentCase() {
        String text = "Las 3 am.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseSensitive(
            "am",
            "AM (sigla), a. m. (hora), am (idioma)"
        );
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(6)
            .text("am")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("am")
            .suggestions(
                List.of(
                    ReplacementSuggestion.of("am", "idioma"),
                    ReplacementSuggestion.of("AM", "sigla"),
                    ReplacementSuggestion.of("a. m.", "hora")
                )
            )
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testUppercaseCaseSensitiveSuggestionsDifferentCase() {
        String text = "En Julio.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseSensitive("Julio", "julio (mes), Julio (nombre)");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("Julio")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("Julio")
            .suggestions(List.of(ReplacementSuggestion.of("Julio", "nombre"), ReplacementSuggestion.of("julio", "mes")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testLowercaseMisspellingCaseInsensitive() {
        String text = "En brazil";

        // There are several cases like this in the Spanish list
        // Ideally the word should be lowercase or the misspelling should be case-sensitive
        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("Brazil", "Brasil");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("brazil")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("Brazil")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("Brasil")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }
}
