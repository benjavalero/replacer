package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.finder.Suggestion;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class MisspellingTest {

    @Test
    public void testParseSimpleSuggestion() {
        Misspelling misspelling = Misspelling.ofCaseInsensitive("a", "A");

        List<Suggestion> suggestions = misspelling.getSuggestions();

        Assert.assertEquals(1, suggestions.size());
        Assert.assertEquals("A", suggestions.get(0).getText());
        Assert.assertEquals("", suggestions.get(0).getComment());
    }

    @Test
    public void testParseComposedSuggestion() {
        Misspelling misspelling = Misspelling.ofCaseInsensitive("a", "A, B, C");

        List<Suggestion> suggestions = misspelling.getSuggestions();

        Assert.assertEquals(3, suggestions.size());
        Assert.assertEquals("A", suggestions.get(0).getText());
        Assert.assertEquals("B", suggestions.get(1).getText());
        Assert.assertEquals("C", suggestions.get(2).getText());
    }

    @Test
    public void testParseComposedSuggestionWithComments() {
        Misspelling misspelling = Misspelling.ofCaseInsensitive("a", "A (D), B (E), C (F)");

        List<Suggestion> suggestions = misspelling.getSuggestions();

        Assert.assertEquals(3, suggestions.size());
        Assert.assertEquals("A", suggestions.get(0).getText());
        Assert.assertEquals("D", suggestions.get(0).getComment());
        Assert.assertEquals("B", suggestions.get(1).getText());
        Assert.assertEquals("E", suggestions.get(1).getComment());
        Assert.assertEquals("C", suggestions.get(2).getText());
        Assert.assertEquals("F", suggestions.get(2).getComment());
    }

    @Test
    public void testParseComposedSuggestionWithCommentsAndCommas() {
        Misspelling misspelling = Misspelling.ofCaseInsensitive("a", "A (D, G), B (E, H), C (F, I)");

        List<Suggestion> suggestions = misspelling.getSuggestions();

        Assert.assertEquals(3, suggestions.size());
        Assert.assertEquals("A", suggestions.get(0).getText());
        Assert.assertEquals("D, G", suggestions.get(0).getComment());
        Assert.assertEquals("B", suggestions.get(1).getText());
        Assert.assertEquals("E, H", suggestions.get(1).getComment());
        Assert.assertEquals("C", suggestions.get(2).getText());
        Assert.assertEquals("F, I", suggestions.get(2).getComment());
    }

    @Test
    public void testParseSuggestionWithExplanationBefore() {
        Misspelling misspelling = Misspelling.ofCaseInsensitive("a", "(B) A");

        List<Suggestion> suggestions = misspelling.getSuggestions();

        Assert.assertEquals(1, suggestions.size());
        Assert.assertEquals("A", suggestions.get(0).getText());
        Assert.assertEquals("B", suggestions.get(0).getComment());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMisspellingWithNullComment() {
        Misspelling.ofCaseInsensitive("A", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMisspellingWithEmptyComment() {
        Misspelling.ofCaseInsensitive("A", "");
    }
}
