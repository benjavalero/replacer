package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.Suggestion;
import java.beans.PropertyChangeEvent;
import java.util.*;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MisspellingComposedFinderTest {

    private MisspellingComposedFinder misspellingComposedFinder;

    @BeforeEach
    public void setUp() {
        misspellingComposedFinder = new MisspellingComposedFinder();
    }

    private void fakeUpdateMisspellingList(List<Misspelling> misspellings) {
        SetValuedMap<WikipediaLanguage, Misspelling> map = new HashSetValuedHashMap<>();
        map.putAll(WikipediaLanguage.SPANISH, misspellings);

        SetValuedMap<WikipediaLanguage, Misspelling> emptyMap = new HashSetValuedHashMap<>();
        misspellingComposedFinder.propertyChange(new PropertyChangeEvent(this, "name", emptyMap, map));
    }

    @Test
    void testNoResults() {
        String text = "Un texto";

        Misspelling misspelling = Misspelling.ofCaseInsensitive("aún así", "aun así");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void testOneResult() {
        String text = "Y aún así vino.";

        Misspelling misspelling = Misspelling.ofCaseInsensitive("aún así", "aun así");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(2)
            .text("aún así")
            .type(ReplacementType.MISSPELLING_COMPOSED)
            .subtype("aún así")
            .suggestions(List.of(Suggestion.ofNoComment("aun así")))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testNested() {
        String text = "Y aún así vino.";

        Misspelling simple = Misspelling.ofCaseInsensitive("aún", "aun");
        Misspelling composed = Misspelling.ofCaseInsensitive("aún así", "aun así");
        this.fakeUpdateMisspellingList(List.of(simple, composed));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(2)
            .text("aún así")
            .type(ReplacementType.MISSPELLING_COMPOSED)
            .subtype("aún así")
            .suggestions(List.of(Suggestion.ofNoComment("aun así")))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testMisspellingWithDot() {
        String text = "Aún mas. Masa.";

        Misspelling misspelling = Misspelling.ofCaseInsensitive("mas.", "más.");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(4)
            .text("mas.")
            .type(ReplacementType.MISSPELLING_COMPOSED)
            .subtype("mas.")
            .suggestions(List.of(Suggestion.ofNoComment("más.")))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testMisspellingWithComma() {
        String text = "Más aun, dos.";

        Misspelling misspelling = Misspelling.ofCaseInsensitive("aun,", "aún,");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(4)
            .text("aun,")
            .type(ReplacementType.MISSPELLING_COMPOSED)
            .subtype("aun,")
            .suggestions(List.of(Suggestion.ofNoComment("aún,")))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testMisspellingWithNumber() {
        String text = "En Rio 2016.";

        Misspelling misspelling = Misspelling.ofCaseSensitive("Rio 2016", "Río 2016");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Replacement expected = Replacement
            .builder()
            .start(3)
            .text("Rio 2016")
            .type(ReplacementType.MISSPELLING_COMPOSED)
            .subtype("Rio 2016")
            .suggestions(List.of(Suggestion.ofNoComment("Río 2016")))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }

    @Test
    void testCaseSensitiveUppercaseToLowercase() {
        String text = "Parque Nacional de Doñana";

        Misspelling misspelling = Misspelling.ofCaseSensitive("Parque Nacional", "parque nacional");
        this.fakeUpdateMisspellingList(List.of(misspelling));

        List<Replacement> results = misspellingComposedFinder.findList(text);

        Suggestion suggestion = Suggestion.ofNoComment("parque nacional");
        Replacement expected = Replacement
            .builder()
            .start(0)
            .text("Parque Nacional")
            .type(ReplacementType.MISSPELLING_COMPOSED)
            .subtype("Parque Nacional")
            .suggestions(List.of(suggestion, suggestion.toUppercase()))
            .build();
        Assertions.assertEquals(Set.of(expected), new HashSet<>(results));
    }
}
