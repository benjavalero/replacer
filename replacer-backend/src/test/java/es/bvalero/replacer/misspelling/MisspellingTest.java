package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.finder.ReplacementSuggestion;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class MisspellingTest {

    @Test
    public void testParseSuggestionsFromComment() {
        Misspelling misspelling1 = Misspelling.ofCaseInsensitive("renuncio",
                "renunció (3.ª persona), renuncio (1.ª persona)");
        List<ReplacementSuggestion> suggestions1 = misspelling1.getSuggestions();
        Assert.assertEquals(2, suggestions1.size());
        Assert.assertEquals("renunció", suggestions1.get(0).getText());
        Assert.assertEquals("3.ª persona", suggestions1.get(0).getComment());

        Misspelling misspelling3 = Misspelling.ofCaseInsensitive("desempeño",
                "desempeño (sustantivo o verbo, 1.ª persona), desempeñó (verbo, 3.ª persona)");
        List<ReplacementSuggestion> suggestions3 = misspelling3.getSuggestions();
        Assert.assertEquals(2, suggestions3.size());
        Assert.assertEquals("desempeño", suggestions3.get(0).getText());
        Assert.assertEquals("sustantivo o verbo, 1.ª persona", suggestions3.get(0).getComment());

        Misspelling misspelling4 = Misspelling.ofCaseInsensitive("k",
                "k (letra), que, qué, kg (kilogramo)");
        List<ReplacementSuggestion> suggestions4 = misspelling4.getSuggestions();
        Assert.assertEquals(4, suggestions4.size());
        Assert.assertEquals("kg", suggestions4.get(3).getText());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMisspellingWithNullComment() {
        Misspelling.ofCaseInsensitive("A", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMisspellingWithEmptyComment() {
        Misspelling.ofCaseInsensitive("A", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMisspellingNotValidComment() {
        Misspelling.ofCaseInsensitive("cidí", "cedé, CD, (disco) compacto");
    }

}
