package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.MisspellingManager;
import es.bvalero.replacer.finder.listing.Suggestion;
import java.beans.PropertyChangeEvent;
import java.util.*;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MisspellingSimpleFinderTest {

    private MisspellingManager misspellingManager;
    private MisspellingSimpleFinder misspellingFinder;

    @BeforeEach
    void setUp() {
        misspellingManager = new MisspellingManager();
        misspellingFinder = new MisspellingSimpleFinder();
        misspellingFinder.setMisspellingManager(misspellingManager);
    }

    private void fakeUpdateMisspellingList(List<Misspelling> misspellings) {
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellings);

        misspellingManager.setItems(map);
        SetValuedMap<WikipediaLanguage, Misspelling> emptyMap = new HashSetValuedHashMap<>();
        misspellingFinder.propertyChange(
            new PropertyChangeEvent(this, MisspellingManager.PROPERTY_ITEMS, emptyMap, map)
        );
    }

    @Test
    void testNoResults() {
        String text = "Un texto";

        Misspelling misspelling = Misspelling.ofCaseInsensitive("abadia", "abadía");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void testOneResult() {
        String text = "Una abadia.";

        Misspelling misspelling = Misspelling.ofCaseInsensitive("abadia", "abadía");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(4)
            .text("abadia")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("abadia")
            .suggestions(List.of(Suggestion.ofNoComment("abadía")))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testTwoResults() {
        String text = "Una abadia online.";

        Misspelling misspelling1 = Misspelling.ofCaseInsensitive("abadia", "abadía");
        Misspelling misspelling2 = Misspelling.ofCaseInsensitive("online", "en línea");
        this.fakeUpdateMisspellingList(List.of(misspelling1, misspelling2));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected1 = Replacement
            .builder()
            .start(4)
            .text("abadia")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("abadia")
            .suggestions(List.of(Suggestion.ofNoComment("abadía")))
            .build();
        Replacement expected2 = Replacement
            .builder()
            .start(11)
            .text("online")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("online")
            .suggestions(List.of(Suggestion.ofNoComment("en línea")))
            .build();
        Assertions.assertEquals(Set.of(expected1, expected2), new HashSet<>(results));
    }

    @Test
    void testUppercase() {
        String text = "Una Abadia.";

        Misspelling misspelling = Misspelling.ofCaseInsensitive("abadia", "abadía");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(4)
            .text("Abadia")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("abadia")
            .suggestions(List.of(Suggestion.ofNoComment("Abadía")))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveUppercaseVersusLowercase() {
        String text = "En enero.";

        Misspelling misspelling = Misspelling.ofCaseSensitive("Enero", "enero");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Assertions.assertEquals(Collections.emptySet(), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveUppercase() {
        String text = "En Brazil.";

        Misspelling misspelling = Misspelling.ofCaseSensitive("Brazil", "Brasil");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("Brazil")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("Brazil")
            .suggestions(List.of(Suggestion.ofNoComment("Brasil")))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveUppercaseToLowercase() {
        String text = "En Enero.";

        Misspelling misspelling = Misspelling.ofCaseSensitive("Enero", "enero");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Suggestion suggestion = Suggestion.ofNoComment("enero");
        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("Enero")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("Enero")
            .suggestions(List.of(suggestion.toUppercase(), suggestion))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveLowercaseVersusUppercase() {
        String text = "En Angola.";

        Misspelling misspelling = Misspelling.ofCaseSensitive("angola", "Angola");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Assertions.assertEquals(Collections.emptySet(), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveLowercase() {
        String text = "En ves.";

        Misspelling misspelling = Misspelling.ofCaseSensitive("ves", "vez");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("ves")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("ves")
            .suggestions(List.of(Suggestion.ofNoComment("vez")))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveLowercaseToUppercase() {
        String text = "En angola.";

        Misspelling misspelling = Misspelling.ofCaseSensitive("angola", "Angola");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("angola")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("angola")
            .suggestions(List.of(Suggestion.ofNoComment("Angola")))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testCompleteWord() {
        String text = "Un texto";

        Misspelling misspelling = Misspelling.ofCaseInsensitive("text", "texto");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void testAllUppercaseCaseInsensitive() {
        String text = "Una ABADIA.";

        Misspelling misspelling = Misspelling.ofCaseInsensitive("abadia", "abadía");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void testAllUppercaseCaseSensitive() {
        String text = "Un OVNI.";

        Misspelling misspelling = Misspelling.ofCaseSensitive("OVNI", "ovni");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("OVNI")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("OVNI")
            .suggestions(List.of(Suggestion.ofNoComment("ovni")))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testBetweenUnderscores() {
        String text = "A _Text Text_ _Text_ Text.";

        Misspelling misspelling = Misspelling.ofCaseInsensitive("text", "texto");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(21)
            .text("Text")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("text")
            .suggestions(List.of(Suggestion.ofNoComment("Texto")))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testSameWordFirst() {
        String text = "Un entreno.";

        String word = "entreno";
        String comment = "entrenó (verbo), entreno (sustantivo)";
        Misspelling misspelling = Misspelling.ofCaseInsensitive(word, comment);
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("entreno")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("entreno")
            .suggestions(List.of(Suggestion.of("entreno", "sustantivo"), Suggestion.of("entrenó", "verbo")))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testMisspellingWithDot() {
        String text = "De 23 cms. de altura";

        Misspelling misspelling = Misspelling.ofCaseInsensitive("cms.", "cm");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void testMisspellingWithNumber() {
        String text = "De 23 m2 de extensión";

        Misspelling misspelling = Misspelling.ofCaseInsensitive("m2", "m²");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void testSuggestionsDifferentCaseLowercase() {
        String text = "Un avion.";

        Misspelling misspelling = Misspelling.ofCaseInsensitive("avion", "avión (aeronave), Avión (río)");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("avion")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("avion")
            .suggestions(List.of(Suggestion.of("avión", "aeronave"), Suggestion.of("Avión", "río")))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testSuggestionsDifferentCaseUppercase() {
        String text = "Un Avion.";

        Misspelling misspelling = Misspelling.ofCaseInsensitive("avion", "avión (aeronave), Avión (río)");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("Avion")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("avion")
            .suggestions(List.of(Suggestion.of("Avión", "aeronave"), Suggestion.of("Avión", "río")))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testLowercaseCaseSensitiveSuggestionsDifferentCase() {
        String text = "Las 3 am.";

        Misspelling misspelling = Misspelling.ofCaseSensitive("am", "AM (sigla), a. m. (hora), am (idioma)");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(6)
            .text("am")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("am")
            .suggestions(
                List.of(Suggestion.of("am", "idioma"), Suggestion.of("AM", "sigla"), Suggestion.of("a. m.", "hora"))
            )
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testUppercaseCaseSensitiveSuggestionsDifferentCase() {
        String text = "En Julio.";

        Misspelling misspelling = Misspelling.ofCaseSensitive("Julio", "julio (mes), Julio (nombre)");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("Julio")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("Julio")
            .suggestions(List.of(Suggestion.of("Julio", "nombre"), Suggestion.of("julio", "mes")))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testLowercaseMisspellingCaseInsensitive() {
        String text = "En brazil";

        // There are several cases like this in the Spanish list
        // Ideally the word should be lowercase or the misspelling should be case-sensitive
        Misspelling misspelling = Misspelling.ofCaseInsensitive("Brazil", "Brasil");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("brazil")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("Brazil")
            .suggestions(List.of(Suggestion.ofNoComment("Brasil")))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }
}
