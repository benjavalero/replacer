package es.bvalero.replacer.misspelling;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class MisspellingTest {

    @Test
    public void testParseSuggestionsFromComment() {
        Misspelling misspelling1 = Misspelling.builder().setWord("renuncio")
                .setComment("renunció (3.ª persona), renuncio (1.ª persona)").build();
        List<String> suggestions1 = misspelling1.getSuggestions();
        Assert.assertEquals(1, suggestions1.size());
        Assert.assertEquals("renunció", suggestions1.get(0));

        Misspelling misspelling2 = Misspelling.builder().setWord("remake")
                .setComment("(nueva) versión o adaptación").build();
        List<String> suggestions2 = misspelling2.getSuggestions();
        Assert.assertEquals(1, suggestions2.size());
        Assert.assertEquals("versión o adaptación", suggestions2.get(0));

        Misspelling misspelling3 = Misspelling.builder().setWord("desempeño")
                .setComment("desempeño (sustantivo o verbo, 1.ª persona), desempeñó (verbo, 3.ª persona)").build();
        List<String> suggestions3 = misspelling3.getSuggestions();
        Assert.assertEquals(1, suggestions3.size());
        Assert.assertEquals("desempeñó", suggestions3.get(0));

        Misspelling misspelling4 = Misspelling.builder().setWord("k")
                .setComment("k (letra), que, qué, kg (kilogramo)").build();
        List<String> suggestions4 = misspelling4.getSuggestions();
        Assert.assertEquals(3, suggestions4.size());
    }

}
