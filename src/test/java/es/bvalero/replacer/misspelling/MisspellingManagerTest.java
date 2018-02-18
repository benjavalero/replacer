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

import java.util.Objects;

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
    public void testFindWikipediaMisspellingsWithErrors() throws Exception {
        Mockito.when(wikipediaService.getArticleContent(Mockito.anyString())).thenThrow(new WikipediaException());

        misspellingManager.updateMisspellings();
    }

    @Test
    public void testUpdateMisspellings() throws WikipediaException {
        String misspellingListText = "Texto\n" +
                "\n" +
                "A||B\n" +
                " C|cs|D\n" +
                " E|CS|F\n" + // No case sensitive
                " G|H\n" +
                " I||J\n" +
                " k||k (letra), que, qué, kg (kilogramo)\n" +
                " I||J\n" + // Duplicated
                " renuncio||renunció (3.ª persona), renuncio (1.ª persona)\n" +
                " remake||(nueva) versión o adaptación\n" +
                " desempeño||desempeño (sustantivo o verbo, 1.ª persona), desempeñó (verbo, 3.ª persona)";

        Mockito.when(wikipediaService.getArticleContent(Mockito.anyString())).thenReturn(misspellingListText);

        misspellingManager.updateMisspellings();

        Assert.assertNull(misspellingManager.findMisspellingByWord("A"));

        Misspelling misspellingC = misspellingManager.findMisspellingByWord("C");
        Assert.assertNotNull(misspellingC);
        Assert.assertEquals("C", misspellingC.getWord());
        Assert.assertTrue(misspellingC.isCaseSensitive());
        Assert.assertEquals("D", misspellingC.getComment());

        Misspelling misspellingE = misspellingManager.findMisspellingByWord("E");
        Assert.assertNotNull(misspellingE);
        Assert.assertEquals("E", misspellingE.getWord());
        Assert.assertTrue(misspellingE.isCaseSensitive());
        Assert.assertEquals("F", misspellingE.getComment());

        Assert.assertNull(misspellingManager.findMisspellingByWord("G"));

        Misspelling misspellingI = misspellingManager.findMisspellingByWord("I");
        Assert.assertNotNull(misspellingI);
        Assert.assertEquals("i", misspellingI.getWord());
        Assert.assertFalse(misspellingI.isCaseSensitive());
        Assert.assertEquals("J", misspellingI.getComment());

        Misspelling misspellingK = misspellingManager.findMisspellingByWord("K");
        Assert.assertNotNull(misspellingK);
        Assert.assertEquals("k", misspellingK.getWord());
        Assert.assertEquals(3, misspellingK.getSuggestions().size());
        Assert.assertTrue(misspellingK.getSuggestions().contains("qué"));
        Assert.assertFalse(misspellingK.getSuggestions().contains("k"));

        Misspelling misspellingRenuncio = misspellingManager.findMisspellingByWord("renuncio");
        Assert.assertNotNull(misspellingRenuncio);
        Assert.assertEquals(1, misspellingRenuncio.getSuggestions().size());
        Assert.assertEquals("renunció", misspellingRenuncio.getSuggestions().get(0));

        Misspelling misspellingRemake = misspellingManager.findMisspellingByWord("remake");
        Assert.assertNotNull(misspellingRemake);
        Assert.assertFalse(misspellingRemake.getSuggestions().isEmpty());
        Assert.assertEquals("versión o adaptación", misspellingRemake.getSuggestions().get(0));

        // Test with commas between brackets
        Misspelling misspellingDesempeno = misspellingManager.findMisspellingByWord("desempeño");
        Assert.assertNotNull(misspellingDesempeno);
        Assert.assertEquals(1, misspellingDesempeno.getSuggestions().size());
        Assert.assertEquals("desempeñó", misspellingDesempeno.getSuggestions().get(0));
    }

    @Test
    public void testFindMisspellingByWord() throws WikipediaException {
        String wikiText = " conprar||comprar\n madrid|cs|Madrid\n álvaro|cs|Álvaro";
        Mockito.when(wikipediaService.getArticleContent(Mockito.anyString())).thenReturn(wikiText);

        Assert.assertEquals("conprar",
                Objects.requireNonNull(misspellingManager.findMisspellingByWord("conprar")).getWord());
        Assert.assertEquals("conprar",
                Objects.requireNonNull(misspellingManager.findMisspellingByWord("Conprar")).getWord());
        Assert.assertEquals("madrid",
                Objects.requireNonNull(misspellingManager.findMisspellingByWord("madrid")).getWord());
        Assert.assertEquals("álvaro",
                Objects.requireNonNull(misspellingManager.findMisspellingByWord("álvaro")).getWord());
        Assert.assertNull(misspellingManager.findMisspellingByWord("Madrid"));
        Assert.assertNull(misspellingManager.findMisspellingByWord("Álvaro"));
    }

}
