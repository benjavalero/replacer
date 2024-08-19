package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.listing.ComposedMisspelling;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MisspellingComposedFinderTest {

    // Dependency injection
    private ListingFinder listingFinder;
    private ComposedMisspellingParser composedMisspellingParser;
    private ComposedMisspellingLoader composedMisspellingLoader;
    private MisspellingComposedFinder misspellingComposedFinder;

    @BeforeEach
    public void setUp() {
        listingFinder = mock(ListingFinder.class);
        composedMisspellingParser = new ComposedMisspellingParser();
        composedMisspellingLoader = new ComposedMisspellingLoader(listingFinder, composedMisspellingParser);
        misspellingComposedFinder = new MisspellingComposedFinder(composedMisspellingLoader);
    }

    private void fakeUpdateMisspellingList(List<ComposedMisspelling> misspellings) {
        SetValuedMap<WikipediaLanguage, ComposedMisspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellings);

        composedMisspellingLoader.setItems(map);
        SetValuedMap<WikipediaLanguage, ComposedMisspelling> emptyMap = new HashSetValuedHashMap<>();
        misspellingComposedFinder.propertyChange(
            new PropertyChangeEvent(this, ComposedMisspellingLoader.LABEL_COMPOSED_MISSPELLING, emptyMap, map)
        );
    }

    @Test
    void testNoResults() {
        String text = "Un texto";

        ComposedMisspelling misspelling = ComposedMisspelling.of("aún así", false, "aun así");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        assertTrue(results.isEmpty());
    }

    @Test
    void testOneResult() {
        String text = "Y aún así vino.";

        ComposedMisspelling misspelling = ComposedMisspelling.of("aún así", false, "aun así");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement.builder()
            .start(2)
            .text("aún así")
            .type(StandardType.of(ReplacementKind.COMPOSED, "aún así"))
            .suggestions(List.of(Suggestion.ofNoComment("aún así"), Suggestion.ofNoComment("aun así")))
            .build();
        assertTrue(Replacement.compareReplacements(Set.of(expected), results));
    }

    @Test
    void testNested() {
        String text = "Y aún así vino.";

        ComposedMisspelling simple = ComposedMisspelling.of("aún", false, "aun");
        ComposedMisspelling composed = ComposedMisspelling.of("aún así", false, "aun así");
        fakeUpdateMisspellingList(List.of(simple, composed));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement.builder()
            .start(2)
            .text("aún así")
            .type(StandardType.of(ReplacementKind.COMPOSED, "aún así"))
            .suggestions(List.of(Suggestion.ofNoComment("aún así"), Suggestion.ofNoComment("aun así")))
            .build();
        assertTrue(Replacement.compareReplacements(Set.of(expected), results));
    }

    @Test
    void testMisspellingWithDot() {
        String text = "Aún mas. Masa.";

        ComposedMisspelling misspelling = ComposedMisspelling.of("mas.", false, "más.");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement.builder()
            .start(4)
            .text("mas.")
            .type(StandardType.of(ReplacementKind.COMPOSED, "mas."))
            .suggestions(List.of(Suggestion.ofNoComment("mas."), Suggestion.ofNoComment("más.")))
            .build();
        assertTrue(Replacement.compareReplacements(Set.of(expected), results));
    }

    @Test
    void testMisspellingWithComma() {
        String text = "Más aun, dos.";

        ComposedMisspelling misspelling = ComposedMisspelling.of("aun,", false, "aún,");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement.builder()
            .start(4)
            .text("aun,")
            .type(StandardType.of(ReplacementKind.COMPOSED, "aun,"))
            .suggestions(List.of(Suggestion.ofNoComment("aun,"), Suggestion.ofNoComment("aún,")))
            .build();
        assertTrue(Replacement.compareReplacements(Set.of(expected), results));
    }

    @Test
    void testMisspellingWithNumber() {
        String text = "En Rio 2016.";

        ComposedMisspelling misspelling = ComposedMisspelling.of("Rio 2016", false, "Río 2016");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement.builder()
            .start(3)
            .text("Rio 2016")
            .type(StandardType.of(ReplacementKind.COMPOSED, "Rio 2016"))
            .suggestions(List.of(Suggestion.ofNoComment("Rio 2016"), Suggestion.ofNoComment("Río 2016")))
            .build();
        assertTrue(Replacement.compareReplacements(Set.of(expected), results));
    }

    @Test
    void testMisspellingIncomplete() {
        String text = "En Rio 2016.";

        ComposedMisspelling misspelling = ComposedMisspelling.of("Rio 20", false, "Río 20");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        assertTrue(results.isEmpty());
    }

    @Test
    void testCaseSensitiveUppercaseToLowercase() {
        String text = "Parque Nacional de Doñana";

        ComposedMisspelling misspelling = ComposedMisspelling.of("Parque Nacional", true, "parque nacional");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement.builder()
            .start(0)
            .text("Parque Nacional")
            .type(StandardType.of(ReplacementKind.COMPOSED, "Parque Nacional"))
            .suggestions(
                List.of(
                    Suggestion.ofNoComment("Parque Nacional"),
                    Suggestion.ofNoComment("parque nacional"),
                    Suggestion.ofNoComment("Parque nacional")
                )
            )
            .build();
        assertTrue(Replacement.compareReplacements(Set.of(expected), results));
    }

    @Test
    void testMisspellingWithSuperIndex() {
        String text = "El 1º.";

        ComposedMisspelling misspelling = ComposedMisspelling.of("1º", false, "1.º");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement.builder()
            .start(3)
            .text("1º")
            .type(StandardType.of(ReplacementKind.COMPOSED, "1º"))
            .suggestions(List.of(Suggestion.ofNoComment("1º"), Suggestion.ofNoComment("1.º")))
            .build();
        assertTrue(Replacement.compareReplacements(Set.of(expected), results));
    }

    @Test
    void testMisspellingWithDegree() {
        String text = "El 1°.";

        ComposedMisspelling misspelling = ComposedMisspelling.of("1°", false, "1.º");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement.builder()
            .start(3)
            .text("1°")
            .type(StandardType.of(ReplacementKind.COMPOSED, "1°"))
            .suggestions(List.of(Suggestion.ofNoComment("1°"), Suggestion.ofNoComment("1.º")))
            .build();
        assertTrue(Replacement.compareReplacements(Set.of(expected), results));
    }

    @Test
    void testMisspellingWithDash() {
        String text = "El anti-ruso.";

        ComposedMisspelling misspelling = ComposedMisspelling.of("anti-ruso", false, "antirruso");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement.builder()
            .start(3)
            .text("anti-ruso")
            .type(StandardType.of(ReplacementKind.COMPOSED, "anti-ruso"))
            .suggestions(List.of(Suggestion.ofNoComment("anti-ruso"), Suggestion.ofNoComment("antirruso")))
            .build();
        assertTrue(Replacement.compareReplacements(Set.of(expected), results));
    }

    @Test
    void testMisspellingWithApostrophe() {
        String text = "En C's.";

        ComposedMisspelling misspelling = ComposedMisspelling.of("C's", true, "Cs");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement.builder()
            .start(3)
            .text("C's")
            .type(StandardType.of(ReplacementKind.COMPOSED, "C's"))
            .suggestions(List.of(Suggestion.ofNoComment("C's"), Suggestion.ofNoComment("Cs")))
            .build();
        assertTrue(Replacement.compareReplacements(Set.of(expected), results));
    }

    @Test
    void testMisspellingWithBrackets() {
        String text = "El [[siglo X]].";

        ComposedMisspelling misspelling = ComposedMisspelling.of("[[siglo X]]", true, "{{siglo|X||s|1}}");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement.builder()
            .start(3)
            .text("[[siglo X]]")
            .type(StandardType.of(ReplacementKind.COMPOSED, "[[siglo X]]"))
            .suggestions(List.of(Suggestion.ofNoComment("[[siglo X]]"), Suggestion.ofNoComment("{{siglo|X||s|1}}")))
            .build();
        assertTrue(Replacement.compareReplacements(Set.of(expected), results));
    }

    @Test
    void testNestedWithLink() {
        String text = "El [[siglo XXI]].";

        ComposedMisspelling cm1 = ComposedMisspelling.of("siglo XXI", true, "{{siglo|XXI||s}}");
        ComposedMisspelling cm2 = ComposedMisspelling.of("[[siglo XXI]]", true, "{{siglo|XXI||s|1}}");
        fakeUpdateMisspellingList(List.of(cm1, cm2));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement.builder()
            .start(3)
            .text(cm2.getWord())
            .type(StandardType.of(ReplacementKind.COMPOSED, cm2.getWord()))
            .suggestions(
                List.of(
                    Suggestion.ofNoComment(cm2.getWord()),
                    Suggestion.ofNoComment(cm2.getSuggestions().get(0).getText())
                )
            )
            .build();
        assertTrue(Replacement.compareReplacements(Set.of(expected), results));
    }

    @Test
    void testMisspellingWithSingleQuotes() {
        String text = "'''Norteamérica''', es un continente para el rodeo.";

        ComposedMisspelling misspelling1 = ComposedMisspelling.of("''', es", true, "''' es");
        ComposedMisspelling misspelling2 = ComposedMisspelling.of("a el", false, "al");
        fakeUpdateMisspellingList(List.of(misspelling1, misspelling2));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement.builder()
            .start(15)
            .text("''', es")
            .type(StandardType.of(ReplacementKind.COMPOSED, "''', es"))
            .suggestions(List.of(Suggestion.ofNoComment("''', es"), Suggestion.ofNoComment("''' es")))
            .build();
        assertTrue(Replacement.compareReplacements(Set.of(expected), results));
    }

    @Test
    void testMisspellingWithUppercase() {
        String text = "El Anti-Ruso.";

        ComposedMisspelling misspelling = ComposedMisspelling.of("anti-ruso", false, "antirruso");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement.builder()
            .start(3)
            .text("Anti-Ruso")
            .type(StandardType.of(ReplacementKind.COMPOSED, "anti-ruso"))
            .suggestions(List.of(Suggestion.ofNoComment("Anti-Ruso"), Suggestion.ofNoComment("Antirruso")))
            .build();
        assertTrue(Replacement.compareReplacements(Set.of(expected), results));
    }

    @ParameterizedTest
    @CsvSource(value = { "rio de la plata", "rio de la Plata" })
    void testMisspellingWithUppercaseSuggestion(String text) {
        ComposedMisspelling misspelling = ComposedMisspelling.of("rio de la plata", false, "Río de la Plata");
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement.builder()
            .start(0)
            .text(text)
            .type(StandardType.of(ReplacementKind.COMPOSED, "rio de la plata"))
            .suggestions(List.of(Suggestion.ofNoComment(text), Suggestion.ofNoComment("Río de la Plata")))
            .build();
        assertTrue(Replacement.compareReplacements(Set.of(expected), results));
    }

    @Test
    void testMergeSuggestionsWithNoComment() {
        String text = "El Sistema Solar.";

        ComposedMisspelling misspelling = ComposedMisspelling.of(
            "Sistema Solar",
            false,
            "sistema solar, Sistema solar (para inicios de frase o columnas de tablas)"
        );
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement.builder()
            .start(3)
            .text("Sistema Solar")
            .type(StandardType.of(ReplacementKind.COMPOSED, "Sistema Solar"))
            .suggestions(
                List.of(
                    Suggestion.ofNoComment("Sistema Solar"),
                    Suggestion.of("Sistema solar", "para inicios de frase o columnas de tablas")
                )
            )
            .build();
        assertTrue(Replacement.compareReplacements(Set.of(expected), results));
    }

    @Test
    void testMergeSuggestionsWithNoComments() {
        String text = "El Sistema Solar.";

        ComposedMisspelling misspelling = ComposedMisspelling.of(
            "Sistema Solar",
            false,
            "sistema solar, Sistema solar"
        );
        fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement.builder()
            .start(3)
            .text("Sistema Solar")
            .type(StandardType.of(ReplacementKind.COMPOSED, "Sistema Solar"))
            .suggestions(List.of(Suggestion.ofNoComment("Sistema Solar"), Suggestion.ofNoComment("Sistema solar")))
            .build();
        assertTrue(Replacement.compareReplacements(Set.of(expected), results));
    }
}
