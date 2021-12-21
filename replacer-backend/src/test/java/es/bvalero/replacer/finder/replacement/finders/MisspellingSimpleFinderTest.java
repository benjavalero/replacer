package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.Suggestion;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import es.bvalero.replacer.finder.replacement.Replacement;
import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MisspellingSimpleFinderTest {

    private SimpleMisspellingLoader simpleMisspellingLoader;
    private SimpleMisspellingParser simpleMisspellingParser;
    private MisspellingSimpleFinder misspellingFinder;

    @BeforeEach
    void setUp() {
        simpleMisspellingLoader = new SimpleMisspellingLoader();
        simpleMisspellingParser = new SimpleMisspellingParser();
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

    @ParameterizedTest
    @CsvSource(
        {
            "Un texto, abadia||abadía", // Misspelling not existing in the text
            "Un texto, text||texto", // Misspelling is not a complete word
            "Una ABADIA., abadia||abadía", // Word all uppercase and misspelling case-insensitive
            "En enero., Enero|cs|enero", // Lowercase word, uppercase misspelling case-sensitive
            "En Angola, angola|cs|Angola", // Uppercase word, lowercase misspelling case-sensitive
            "Marca.com, com||con", // Word immediately preceded by a dot
        }
    )
    void testNoResults(String text, String misspellingLine) {
        SimpleMisspelling misspelling = simpleMisspellingParser.parseItemLine(misspellingLine);
        assertNotNull(misspelling);
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
            .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "abadia"))
            .suggestions(List.of(Suggestion.ofNoComment("abadía")))
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
            .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "abadia"))
            .suggestions(List.of(Suggestion.ofNoComment("abadía")))
            .build();
        Replacement expected2 = Replacement
            .builder()
            .start(11)
            .text("online")
            .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "online"))
            .suggestions(List.of(Suggestion.ofNoComment("en línea")))
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
            .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "abadia"))
            .suggestions(List.of(Suggestion.ofNoComment("Abadía")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
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
            .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "Brazil"))
            .suggestions(List.of(Suggestion.ofNoComment("Brasil")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveUppercaseToLowercase() {
        String text = "En Enero.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseSensitive("Enero", "enero");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Suggestion suggestion = Suggestion.ofNoComment("enero");
        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("Enero")
            .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "Enero"))
            .suggestions(List.of(MisspellingSimpleFinder.toUppercase(suggestion), suggestion))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
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
            .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "ves"))
            .suggestions(List.of(Suggestion.ofNoComment("vez")))
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
            .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "angola"))
            .suggestions(List.of(Suggestion.ofNoComment("Angola")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
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
            .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "OVNI"))
            .suggestions(List.of(Suggestion.ofNoComment("ovni")))
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
            .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "text"))
            .suggestions(List.of(Suggestion.ofNoComment("Texto")))
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
            .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "entreno"))
            .suggestions(List.of(Suggestion.of("entreno", "sustantivo"), Suggestion.of("entrenó", "verbo")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @ParameterizedTest
    @CsvSource({ "cms., cm", "m2, m²" })
    void testNotValidMisspelling(String word, String comment) {
        assertThrows(IllegalArgumentException.class, () -> SimpleMisspelling.ofCaseInsensitive(word, comment));
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
            .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "avion"))
            .suggestions(List.of(Suggestion.of("avión", "aeronave"), Suggestion.of("Avión", "río")))
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
            .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "avion"))
            .suggestions(List.of(Suggestion.of("Avión", "aeronave"), Suggestion.of("Avión", "río")))
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
            .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "am"))
            .suggestions(
                List.of(Suggestion.of("am", "idioma"), Suggestion.of("AM", "sigla"), Suggestion.of("a. m.", "hora"))
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
            .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "Julio"))
            .suggestions(List.of(Suggestion.of("Julio", "nombre"), Suggestion.of("julio", "mes")))
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
            .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "Brazil"))
            .suggestions(List.of(Suggestion.ofNoComment("Brasil")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testFindMatchingReplacementType() {
        final WikipediaLanguage lang = WikipediaLanguage.getDefault();
        final ReplacementKind simple = ReplacementKind.MISSPELLING_SIMPLE;

        SimpleMisspelling misspelling1 = SimpleMisspelling.of("accion", false, "acción");
        SimpleMisspelling misspelling2 = SimpleMisspelling.of("Enero", true, "enero");
        SimpleMisspelling misspelling3 = SimpleMisspelling.of("madrid", true, "Madrid");
        this.fakeUpdateMisspellingList(List.of(misspelling1, misspelling2, misspelling3));

        // Case-insensitive: accion||acción
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "accion", true));
        assertEquals(
            Optional.of(ReplacementType.of(simple, "accion")),
            misspellingFinder.findMatchingReplacementType(lang, "accion", false)
        );
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "Accion", true));
        assertEquals(
            Optional.of(ReplacementType.of(simple, "accion")),
            misspellingFinder.findMatchingReplacementType(lang, "Accion", false)
        );

        // Case-sensitive uppercase: Enero|cs|enero
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "enero", true));
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "enero", false));
        assertEquals(
            Optional.of(ReplacementType.of(simple, "Enero")),
            misspellingFinder.findMatchingReplacementType(lang, "Enero", true)
        );
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "Enero", false));

        // Case-sensitive lowercase: madrid|cs|Madrid
        assertEquals(
            Optional.of(ReplacementType.of(simple, "madrid")),
            misspellingFinder.findMatchingReplacementType(lang, "madrid", true)
        );
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "madrid", false));
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "Madrid", true));
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "Madrid", false));
    }
}
