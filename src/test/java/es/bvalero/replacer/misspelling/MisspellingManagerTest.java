package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

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

    @Test
    public void testParseMisspellingList() {
        String content = "Texto\n" +
                "\n" +
                "A||B\n" +
                " C|cs|D\n" +
                " E|CS|F\n" + // No case sensitive
                " G|H\n" +
                " I||J\n" +
                " k||k (letra), que, qué, kg (kilogramo)\n" +
                " I||J\n"; // Duplicated

        List<Misspelling> misspellingList = misspellingManager.parseMisspellingList(content);
        Assert.assertEquals(4, misspellingList.size());

        Assert.assertEquals("C", misspellingList.get(0).getWord());
        Assert.assertTrue(misspellingList.get(0).isCaseSensitive());
        Assert.assertEquals("D", misspellingList.get(0).getComment());

        Assert.assertEquals("E", misspellingList.get(1).getWord());
        Assert.assertTrue(misspellingList.get(1).isCaseSensitive());
        Assert.assertEquals("F", misspellingList.get(1).getComment());

        Assert.assertEquals("i", misspellingList.get(2).getWord());
        Assert.assertFalse(misspellingList.get(2).isCaseSensitive());
        Assert.assertEquals("J", misspellingList.get(2).getComment());

        Assert.assertEquals("k", misspellingList.get(3).getWord());
        Assert.assertEquals(3, misspellingList.get(3).getSuggestions().size());
        Assert.assertTrue(misspellingList.get(3).getSuggestions().contains("qué"));
        Assert.assertFalse(misspellingList.get(3).getSuggestions().contains("k"));
    }

    @Test
    public void testFindMisspellingByWord() throws Exception {
        String wikiText = " conprar||comprar\n madrid|cs|Madrid\n álvaro|cs|Álvaro";
        Mockito.when(wikipediaService.getArticleContent(Mockito.anyString())).thenReturn(wikiText);

        Assert.assertEquals("conprar", misspellingManager.findMisspellingByWord("conprar").getWord());
        Assert.assertEquals("conprar", misspellingManager.findMisspellingByWord("Conprar").getWord());
        Assert.assertEquals("madrid", misspellingManager.findMisspellingByWord("madrid").getWord());
        Assert.assertEquals("álvaro", misspellingManager.findMisspellingByWord("álvaro").getWord());
        Assert.assertNull(misspellingManager.findMisspellingByWord("Madrid"));
        Assert.assertNull(misspellingManager.findMisspellingByWord("Álvaro"));
    }

    @Test
    public void testParseSuggestions() {
        String mainWord = "renuncio";
        String comment = "renunció (3.ª persona), renuncio (1.ª persona)";
        Assert.assertEquals(1, misspellingManager.parseSuggestions(comment, mainWord).size());
        Assert.assertEquals("renunció", misspellingManager.parseSuggestions(comment, mainWord).get(0));

        mainWord = "remake";
        comment = "(nueva) versión o adaptación";
        Assert.assertTrue(misspellingManager.parseSuggestions(comment, mainWord).isEmpty());
    }

}
