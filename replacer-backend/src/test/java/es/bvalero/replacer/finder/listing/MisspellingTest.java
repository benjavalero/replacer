package es.bvalero.replacer.finder.listing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
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
}
