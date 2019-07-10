package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.finder.ReplacementSuggestion;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class MisspellingTest {

    @Test
    public void testParseSuggestionsFromComment() {
        Misspelling misspelling1 = Misspelling.builder().setWord("renuncio")
                .setComment("renunció (3.ª persona), renuncio (1.ª persona)").build();
        List<ReplacementSuggestion> suggestions1 = misspelling1.getSuggestions();
        Assert.assertEquals(2, suggestions1.size());
        Assert.assertEquals("renunció", suggestions1.get(0).getText());
        Assert.assertEquals("3.ª persona", suggestions1.get(0).getComment());

        Misspelling misspelling2 = Misspelling.builder().setWord("cidí")
                .setComment("cedé, CD, (disco) compacto").build();
        List<ReplacementSuggestion> suggestions2 = misspelling2.getSuggestions();
        Assert.assertEquals(3, suggestions2.size());
        Assert.assertEquals("cedé", suggestions2.get(0).getText());
        Assert.assertEquals("CD", suggestions2.get(1).getText());
        Assert.assertEquals("compacto", suggestions2.get(2).getText());

        Misspelling misspelling3 = Misspelling.builder().setWord("desempeño")
                .setComment("desempeño (sustantivo o verbo, 1.ª persona), desempeñó (verbo, 3.ª persona)").build();
        List<ReplacementSuggestion> suggestions3 = misspelling3.getSuggestions();
        Assert.assertEquals(2, suggestions3.size());
        Assert.assertEquals("desempeño", suggestions3.get(0).getText());
        Assert.assertEquals("sustantivo o verbo, 1.ª persona", suggestions3.get(0).getComment());

        Misspelling misspelling4 = Misspelling.builder().setWord("k")
                .setComment("k (letra), que, qué, kg (kilogramo)").build();
        List<ReplacementSuggestion> suggestions4 = misspelling4.getSuggestions();
        Assert.assertEquals(4, suggestions4.size());
        Assert.assertEquals("kg", suggestions4.get(3).getText());
    }

}
