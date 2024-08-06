package es.bvalero.replacer.finder;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.finder.listing.ComposedMisspelling;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MisspellingTest {

    @Test
    void testParseSimpleSuggestion() {
        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("a", "A");

        List<MisspellingSuggestion> suggestions = misspelling.getSuggestions();

        assertEquals(1, suggestions.size());
        assertEquals("A", suggestions.get(0).getText());
        assertNull(suggestions.get(0).getComment());
    }

    @Test
    void testParseComposedSuggestion() {
        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("a", "A, B, C");

        List<MisspellingSuggestion> suggestions = misspelling.getSuggestions();

        assertEquals(3, suggestions.size());
        assertEquals("A", suggestions.get(0).getText());
        assertEquals("B", suggestions.get(1).getText());
        assertEquals("C", suggestions.get(2).getText());
    }

    @Test
    void testParseComposedSuggestionWithComments() {
        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("a", "A (D), B (E), C (F)");

        List<MisspellingSuggestion> suggestions = misspelling.getSuggestions();

        assertEquals(3, suggestions.size());
        assertEquals("A", suggestions.get(0).getText());
        assertEquals("D", suggestions.get(0).getComment());
        assertEquals("B", suggestions.get(1).getText());
        assertEquals("E", suggestions.get(1).getComment());
        assertEquals("C", suggestions.get(2).getText());
        assertEquals("F", suggestions.get(2).getComment());
    }

    @Test
    void testParseComposedSuggestionWithCommentsAndCommas() {
        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("a", "A (D, G), B (E, H), C (F, I)");

        List<MisspellingSuggestion> suggestions = misspelling.getSuggestions();

        assertEquals(3, suggestions.size());
        assertEquals("A", suggestions.get(0).getText());
        assertEquals("D, G", suggestions.get(0).getComment());
        assertEquals("B", suggestions.get(1).getText());
        assertEquals("E, H", suggestions.get(1).getComment());
        assertEquals("C", suggestions.get(2).getText());
        assertEquals("F, I", suggestions.get(2).getComment());
    }

    @Test
    void testParseSuggestionWithExplanationBefore() {
        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("a", "(B) A");

        List<MisspellingSuggestion> suggestions = misspelling.getSuggestions();

        assertEquals(1, suggestions.size());
        assertEquals("A", suggestions.get(0).getText());
        assertEquals("B", suggestions.get(0).getComment());
    }

    @Test
    void testMisspellingWithNullComment() {
        assertThrows(IllegalArgumentException.class, () -> SimpleMisspelling.ofCaseInsensitive("A", null));
    }

    @Test
    void testMisspellingWithEmptyComment() {
        assertThrows(IllegalArgumentException.class, () -> SimpleMisspelling.ofCaseInsensitive("A", ""));
    }

    @Test
    void testSuggestionEqualsWord() {
        assertThrows(IllegalArgumentException.class, () -> SimpleMisspelling.ofCaseInsensitive("a", "a"));
    }

    @Test
    void testSimpleCaseSensitiveTermsLowercase() {
        String word = "paris";
        SimpleMisspelling m = SimpleMisspelling.of(word, true, "X");
        assertEquals(Set.of(word), m.getTerms());
    }

    @Test
    void testSimpleCaseSensitiveTermsUppercase() {
        String word = "Paris";
        SimpleMisspelling m = SimpleMisspelling.of(word, true, "X");
        assertEquals(Set.of(word), m.getTerms());
    }

    @Test
    void testComposedCaseSensitiveTermsLowercase() {
        String word = "ad hoc";
        ComposedMisspelling m = ComposedMisspelling.of(word, true, "X");
        assertEquals(Set.of(word), m.getTerms());
    }

    @Test
    void testComposedCaseSensitiveTermsUppercase() {
        String word = "Administración pública";
        ComposedMisspelling m = ComposedMisspelling.of(word, true, "X");
        assertEquals(Set.of(word), m.getTerms());
    }

    @Test
    void testSimpleCaseInsensitiveTermsLowercase() {
        String word = "paris";
        SimpleMisspelling m = SimpleMisspelling.of(word, false, "X");
        assertEquals(Set.of("paris", "Paris"), m.getTerms());
    }

    @Test
    void testSimpleCaseInsensitiveTermsUppercase() {
        String word = "Paris";
        SimpleMisspelling m = SimpleMisspelling.of(word, false, "X");
        assertEquals(Set.of("paris", "Paris"), m.getTerms());
    }
}
