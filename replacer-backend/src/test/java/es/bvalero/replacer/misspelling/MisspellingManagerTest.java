package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.List;

public class MisspellingManagerTest {

    @Mock
    private IWikipediaFacade wikipediaService;

    @InjectMocks
    private MisspellingManager misspellingManager;

    @Before
    public void setUp() {
        misspellingManager = new MisspellingManager();
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = WikipediaException.class)
    public void testFindWikipediaMisspellingsWithErrors() throws WikipediaException {
        Mockito.when(wikipediaService.getMisspellingListPageContent()).thenThrow(new WikipediaException());
        misspellingManager.findWikipediaMisspellings();
    }

    @Test
    public void testParseMisspellingListText() {
        String misspellingListText = "Texto\n\n" +
                "A||B\n" + // No starting whitespace
                " C|cs|D\n" +
                " E|CS|F\n" +
                " G|H\n" + // Bad formatted
                " I||J\n" +
                " k||k (letra), que, qué, kg (kilogramo)\n" +
                " I||J\n" + // Duplicated
                " I||M\n" + // Duplicated but different comment
                " renuncio||renunció (3.ª persona), renuncio (1.ª persona)\n" +
                " remake||(nueva) versión o adaptación\n" +
                " desempeño||desempeño (sustantivo o verbo, 1.ª persona), desempeñó (verbo, 3.ª persona)";

        Collection<Misspelling> misspellings = misspellingManager.parseMisspellingListText(misspellingListText);
        Assert.assertEquals(7, misspellings.size());

        Assert.assertTrue(misspellings.contains(Misspelling.builder()
                .setWord("C").setCaseSensitive(true).setComment("D").build()));
        Assert.assertTrue(misspellings.contains(Misspelling.builder()
                .setWord("E").setCaseSensitive(true).setComment("F").build()));
        Assert.assertTrue(misspellings.contains(Misspelling.builder()
                .setWord("I").setCaseSensitive(false).setComment("J").build()));
    }

    @Test
    public void testParseSuggestionsFromComment() {
        Misspelling misspelling1 = Misspelling.builder()
                .setWord("renuncio").setComment("renunció (3.ª persona), renuncio (1.ª persona)").build();
        List<String> suggestions1 = misspelling1.getSuggestions();
        Assert.assertEquals(1, suggestions1.size());
        Assert.assertEquals("renunció", suggestions1.get(0));

        Misspelling misspelling2 = Misspelling.builder()
                .setWord("remake").setComment("(nueva) versión o adaptación").build();
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
