package es.bvalero.replacer.finder.listing;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MisspellingTest {

    @Test
    void testParseSimpleSuggestion() {
        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("a", "A");

        List<MisspellingSuggestion> suggestions = misspelling.getSuggestions();

        Assertions.assertEquals(1, suggestions.size());
        Assertions.assertEquals("A", suggestions.get(0).getText());
        Assertions.assertNull(suggestions.get(0).getComment());
    }

    @Test
    void testParseComposedSuggestion() {
        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("a", "A, B, C");

        List<MisspellingSuggestion> suggestions = misspelling.getSuggestions();

        Assertions.assertEquals(3, suggestions.size());
        Assertions.assertEquals("A", suggestions.get(0).getText());
        Assertions.assertEquals("B", suggestions.get(1).getText());
        Assertions.assertEquals("C", suggestions.get(2).getText());
    }

    @Test
    void testParseComposedSuggestionWithComments() {
        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("a", "A (D), B (E), C (F)");

        List<MisspellingSuggestion> suggestions = misspelling.getSuggestions();

        Assertions.assertEquals(3, suggestions.size());
        Assertions.assertEquals("A", suggestions.get(0).getText());
        Assertions.assertEquals("D", suggestions.get(0).getComment());
        Assertions.assertEquals("B", suggestions.get(1).getText());
        Assertions.assertEquals("E", suggestions.get(1).getComment());
        Assertions.assertEquals("C", suggestions.get(2).getText());
        Assertions.assertEquals("F", suggestions.get(2).getComment());
    }

    @Test
    void testParseComposedSuggestionWithCommentsAndCommas() {
        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("a", "A (D, G), B (E, H), C (F, I)");

        List<MisspellingSuggestion> suggestions = misspelling.getSuggestions();

        Assertions.assertEquals(3, suggestions.size());
        Assertions.assertEquals("A", suggestions.get(0).getText());
        Assertions.assertEquals("D, G", suggestions.get(0).getComment());
        Assertions.assertEquals("B", suggestions.get(1).getText());
        Assertions.assertEquals("E, H", suggestions.get(1).getComment());
        Assertions.assertEquals("C", suggestions.get(2).getText());
        Assertions.assertEquals("F, I", suggestions.get(2).getComment());
    }

    @Test
    void testParseSuggestionWithExplanationBefore() {
        SimpleMisspelling misspelling = SimpleMisspelling.ofCaseInsensitive("a", "(B) A");

        List<MisspellingSuggestion> suggestions = misspelling.getSuggestions();

        Assertions.assertEquals(1, suggestions.size());
        Assertions.assertEquals("A", suggestions.get(0).getText());
        Assertions.assertEquals("B", suggestions.get(0).getComment());
    }

    @Test
    void testMisspellingWithNullComment() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> SimpleMisspelling.ofCaseInsensitive("A", null));
    }

    @Test
    void testMisspellingWithEmptyComment() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> SimpleMisspelling.ofCaseInsensitive("A", ""));
    }
}
