package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.ComposedMisspelling;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementSuggestion;
import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MisspellingComposedFinderTest {

    private ComposedMisspellingLoader composedMisspellingLoader;
    private MisspellingComposedFinder misspellingComposedFinder;

    @BeforeEach
    public void setUp() {
        composedMisspellingLoader = new ComposedMisspellingLoader();
        misspellingComposedFinder = new MisspellingComposedFinder();
        misspellingComposedFinder.setComposedMisspellingLoader(composedMisspellingLoader);
    }

    private void fakeUpdateMisspellingList(List<ComposedMisspelling> misspellings) {
        SetValuedMap<WikipediaLanguage, ComposedMisspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellings);

        composedMisspellingLoader.setItems(map);
        SetValuedMap<WikipediaLanguage, ComposedMisspelling> emptyMap = new HashSetValuedHashMap<>();
        misspellingComposedFinder.propertyChange(
            new PropertyChangeEvent(this, ComposedMisspellingLoader.PROPERTY_ITEMS, emptyMap, map)
        );
    }

    @Test
    void testNoResults() {
        String text = "Un texto";

        ComposedMisspelling misspelling = ComposedMisspelling.of("aún así", false, "aun así");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        assertTrue(results.isEmpty());
    }

    @Test
    void testOneResult() {
        String text = "Y aún así vino.";

        ComposedMisspelling misspelling = ComposedMisspelling.of("aún así", false, "aun así");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(2)
            .text("aún así")
            .type(ReplacementType.MISSPELLING_COMPOSED)
            .subtype("aún así")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("aun así")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testNested() {
        String text = "Y aún así vino.";

        ComposedMisspelling simple = ComposedMisspelling.of("aún", false, "aun");
        ComposedMisspelling composed = ComposedMisspelling.of("aún así", false, "aun así");
        this.fakeUpdateMisspellingList(List.of(simple, composed));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(2)
            .text("aún así")
            .type(ReplacementType.MISSPELLING_COMPOSED)
            .subtype("aún así")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("aun así")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testMisspellingWithDot() {
        String text = "Aún mas. Masa.";

        ComposedMisspelling misspelling = ComposedMisspelling.of("mas.", false, "más.");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(4)
            .text("mas.")
            .type(ReplacementType.MISSPELLING_COMPOSED)
            .subtype("mas.")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("más.")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testMisspellingWithComma() {
        String text = "Más aun, dos.";

        ComposedMisspelling misspelling = ComposedMisspelling.of("aun,", false, "aún,");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(4)
            .text("aun,")
            .type(ReplacementType.MISSPELLING_COMPOSED)
            .subtype("aun,")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("aún,")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testMisspellingWithNumber() {
        String text = "En Rio 2016.";

        ComposedMisspelling misspelling = ComposedMisspelling.of("Rio 2016", false, "Río 2016");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("Rio 2016")
            .type(ReplacementType.MISSPELLING_COMPOSED)
            .subtype("Rio 2016")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("Río 2016")))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveUppercaseToLowercase() {
        String text = "Parque Nacional de Doñana";

        ComposedMisspelling misspelling = ComposedMisspelling.of("Parque Nacional", true, "parque nacional");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        ReplacementSuggestion suggestion = ReplacementSuggestion.ofNoComment("parque nacional");
        Replacement expected = Replacement
            .builder()
            .start(0)
            .text("Parque Nacional")
            .type(ReplacementType.MISSPELLING_COMPOSED)
            .subtype("Parque Nacional")
            .suggestions(List.of(suggestion, suggestion.toUppercase()))
            .build();
        assertEquals(Set.of(expected), new HashSet<>(results));
    }
}
