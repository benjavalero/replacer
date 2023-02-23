package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
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
            "20 Km2., Km|cs|km", // Word separated by a digit
            "www.europa.eu, europa|cs|Europa", // URL token
            "google.es/europa/index, europa|cs|Europa", // URL token
        }
    )
    void testNoResults(String text, String misspellingLine) {
        SimpleMisspelling misspelling = simpleMisspellingParser.parseItemLine(misspellingLine);
        assertNotNull(misspelling);
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        assertTrue(results.isEmpty());
    }

    @Test
    void testOneResult() {
        String text = "Una abadia.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("abadia", "abadía");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(4)
            .text("abadia")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "abadia"))
            .suggestions(List.of(Suggestion.ofNoComment("abadia"), Suggestion.ofNoComment("abadía")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testTwoResults() {
        String text = "Una abadia online.";

        SimpleMisspelling misspelling1 = SimpleMisspelling.ofCaseInsensitive("abadia", "abadía");
        SimpleMisspelling misspelling2 = SimpleMisspelling.ofCaseInsensitive("online", "en línea");
        fakeUpdateMisspellingList(List.of(misspelling1, misspelling2));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected1 = Replacement
            .builder()
            .start(4)
            .text("abadia")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "abadia"))
            .suggestions(List.of(Suggestion.ofNoComment("abadia"), Suggestion.ofNoComment("abadía")))
            .build();
        Replacement expected2 = Replacement
            .builder()
            .start(11)
            .text("online")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "online"))
            .suggestions(List.of(Suggestion.ofNoComment("online"), Suggestion.ofNoComment("en línea")))
            .build();
        assertEquals(Set.of(expected1, expected2), new HashSet<>(results));
    }

    @Test
    void testUppercase() {
        String text = "Una Abadia.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("abadia", "abadía");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(4)
            .text("Abadia")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "abadia"))
            .suggestions(List.of(Suggestion.ofNoComment("Abadia"), Suggestion.ofNoComment("Abadía")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveUppercase() {
        String text = "En Brazil.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseSensitive("Brazil", "Brasil");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("Brazil")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "Brazil"))
            .suggestions(List.of(Suggestion.ofNoComment("Brazil"), Suggestion.ofNoComment("Brasil")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveUppercaseToLowercase() {
        String text = "En Enero.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseSensitive("Enero", "enero");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("Enero")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "Enero"))
            .suggestions(List.of(Suggestion.ofNoComment("Enero"), Suggestion.ofNoComment("enero")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveLowercase() {
        String text = "En ves.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseSensitive("ves", "vez");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("ves")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "ves"))
            .suggestions(List.of(Suggestion.ofNoComment("ves"), Suggestion.ofNoComment("vez")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveLowercaseToUppercase() {
        String text = "En angola.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseSensitive("angola", "Angola");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("angola")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "angola"))
            .suggestions(List.of(Suggestion.ofNoComment("angola"), Suggestion.ofNoComment("Angola")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testAllUppercaseCaseSensitive() {
        String text = "Un OVNI.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseSensitive("OVNI", "ovni");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("OVNI")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "OVNI"))
            .suggestions(List.of(Suggestion.ofNoComment("OVNI"), Suggestion.ofNoComment("ovni")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testBetweenUnderscores() {
        String text = "A _Text Text_ _Text_ Text.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("text", "texto");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(21)
            .text("Text")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "text"))
            .suggestions(List.of(Suggestion.ofNoComment("Text"), Suggestion.ofNoComment("Texto")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testSameWordFirst() {
        String text = "Un entreno.";

        String word = "entreno";
        String comment = "entrenó (verbo), entreno (sustantivo)";
        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive(word, comment);
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("entreno")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "entreno"))
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
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("avion")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "avion"))
            .suggestions(
                List.of(
                    Suggestion.ofNoComment("avion"),
                    Suggestion.of("avión", "aeronave"),
                    Suggestion.of("Avión", "río")
                )
            )
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testSuggestionsDifferentCaseUppercase() {
        String text = "Un Avion.";

        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("avion", "avión (aeronave), Avión (río)");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("Avion")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "avion"))
            .suggestions(
                List.of(
                    Suggestion.ofNoComment("Avion"),
                    Suggestion.of("Avión", "aeronave"),
                    Suggestion.of("Avión", "río")
                )
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
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(6)
            .text("am")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "am"))
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
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("Julio")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "Julio"))
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
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("brazil")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "Brazil"))
            .suggestions(List.of(Suggestion.ofNoComment("brazil"), Suggestion.ofNoComment("Brasil")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testMergeSuggestions() {
        String text = "Quinto Vario.";

        // There exist an alternative as a common word and the same one as a proper noun
        // so when the original text is uppercase both alternatives become the same one
        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive(
            "vario",
            "vario (adjetivo), Vario (nombre propio), varío (verbo)"
        );
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(7)
            .text("Vario")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "vario"))
            .suggestions(List.of(Suggestion.of("Vario", "adjetivo; nombre propio"), Suggestion.of("Varío", "verbo")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testMergeSeveralSuggestions() {
        String text = "Santa Barbara.";

        // There exist an alternative as a common word and the same one as a proper noun
        // so when the original text is uppercase both alternatives become the same one
        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive(
            "barbara",
            "bárbara (adjetivo), Bárbara (nombre en español), Barbara (nombre en inglés), barbara (verbo imperfecto), barbará (verbo futuro)"
        );
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(6)
            .text("Barbara")
            .type(ReplacementType.ofType(ReplacementKind.SIMPLE, "barbara"))
            .suggestions(
                List.of(
                    Suggestion.of("Barbara", "nombre en inglés; verbo imperfecto"),
                    Suggestion.of("Bárbara", "adjetivo; nombre en español"),
                    Suggestion.of("Barbará", "verbo futuro")
                )
            )
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testFindMatchingReplacementType() {
        final WikipediaLanguage lang = WikipediaLanguage.getDefault();
        final ReplacementKind simple = ReplacementKind.SIMPLE;

        SimpleMisspelling misspelling1 = SimpleMisspelling.of("accion", false, "acción");
        SimpleMisspelling misspelling2 = SimpleMisspelling.of("Enero", true, "enero");
        SimpleMisspelling misspelling3 = SimpleMisspelling.of("madrid", true, "Madrid");
        SimpleMisspelling misspelling4 = SimpleMisspelling.of("peru", false, "Perú");
        fakeUpdateMisspellingList(List.of(misspelling1, misspelling2, misspelling3, misspelling4));

        // 1. Case-insensitive: accion||acción
        // accion - true  => KO
        // accion - false => OK
        // Accion - true  => KO
        // Accion - false => OK
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "accion", true));
        assertEquals(
            Optional.of(ReplacementType.ofType(simple, "accion")),
            misspellingFinder.findMatchingReplacementType(lang, "accion", false)
        );
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "Accion", true));
        assertEquals(
            Optional.of(ReplacementType.ofType(simple, "accion")),
            misspellingFinder.findMatchingReplacementType(lang, "Accion", false)
        );

        // 2. Case-sensitive uppercase to lowercase: Enero|cs|enero
        // enero - true  => KO
        // enero - false => KO
        // Enero - true  => OK
        // Enero - false => KO
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "enero", true));
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "enero", false));
        assertEquals(
            Optional.of(ReplacementType.ofType(simple, "Enero")),
            misspellingFinder.findMatchingReplacementType(lang, "Enero", true)
        );
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "Enero", false));

        // 3. Case-sensitive lowercase to uppercase: madrid|cs|Madrid
        // madrid - true  => OK
        // madrid - false => KO
        // Madrid - true  => KO
        // Madrid - false => KO
        assertEquals(
            Optional.of(ReplacementType.ofType(simple, "madrid")),
            misspellingFinder.findMatchingReplacementType(lang, "madrid", true)
        );
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "madrid", false));
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "Madrid", true));
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "Madrid", false));

        // 4. Case-insensitive lowercase to uppercase: peru||Peru
        // peru - true  => KO
        // peru - false => OK
        // Peru - true  => KO
        // Peru - false => OK
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "peru", true));
        assertEquals(
            Optional.of(ReplacementType.ofType(simple, "peru")),
            misspellingFinder.findMatchingReplacementType(lang, "peru", false)
        );
        assertEquals(Optional.empty(), misspellingFinder.findMatchingReplacementType(lang, "Peru", true));
        assertEquals(
            Optional.of(ReplacementType.ofType(simple, "peru")),
            misspellingFinder.findMatchingReplacementType(lang, "Peru", false)
        );
    }
}
