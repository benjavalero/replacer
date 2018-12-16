package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import es.bvalero.replacer.wikipedia.WikipediaException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collection;

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

    @Test
    public void testFindWikipediaMisspellingsWithErrors() throws WikipediaException {
        Mockito.when(wikipediaService.getArticleContent(Mockito.anyString())).thenThrow(new WikipediaException());
        Assert.assertTrue(misspellingManager.findWikipediaMisspellings().isEmpty());
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
                " renuncio||renunció (3.ª persona), renuncio (1.ª persona)\n" +
                " remake||(nueva) versión o adaptación\n" +
                " desempeño||desempeño (sustantivo o verbo, 1.ª persona), desempeñó (verbo, 3.ª persona)";

        Collection<Misspelling> misspellings = MisspellingManager.parseMisspellingListText(misspellingListText);
        Assert.assertEquals(7, misspellings.size());

        Assert.assertTrue(misspellings.contains(Misspelling.builder()
                .setWord("C").setCaseSensitive(true).setComment("D").build()));
        Assert.assertTrue(misspellings.contains(Misspelling.builder()
                .setWord("E").setCaseSensitive(true).setComment("F").build()));
        Assert.assertTrue(misspellings.contains(Misspelling.builder()
                .setWord("I").setCaseSensitive(false).setComment("J").build()));
    }

    @Test
    public void testStartsWithUpperCase() {
        Assert.assertTrue(MisspellingManager.startsWithUpperCase("Álvaro"));
        Assert.assertFalse(MisspellingManager.startsWithUpperCase("úlcera"));
    }

}
