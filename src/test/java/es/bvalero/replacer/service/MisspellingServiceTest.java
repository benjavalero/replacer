package es.bvalero.replacer.service;

import es.bvalero.replacer.domain.Misspelling;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class MisspellingServiceTest {

    @Mock
    private WikipediaService wikipediaService;

    @InjectMocks
    private MisspellingService misspellingService;

    @Before
    public void setUp() {
        misspellingService = new MisspellingService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUpdateMisspellingList() {
        String wikiText = " A|cs|Á\n a||á";
        when(wikipediaService.getArticleContent(anyString())).thenReturn(wikiText);

        misspellingService.updateMisspellingList();

        assertEquals(2, misspellingService.getMisspellingMap().size());
        assertTrue(misspellingService.getMisspellingMap().get("A").isCaseSensitive());
        assertFalse(misspellingService.getMisspellingMap().get("a").isCaseSensitive());
    }

    @Test
    public void testParseMisspellingList() {
        String content = "Texto\n" +
                "\n" +
                "A||B\n" +
                " C|cs|D\n" +
                " E|CS|F\n" +
                " G|H\n" +
                " I||J\n" +
                " I||J\n";

        List<Misspelling> misspellingList = misspellingService.parseMisspellingList(content);
        assertEquals(3, misspellingList.size());

        assertEquals("C", misspellingList.get(0).getWord());
        assertTrue(misspellingList.get(0).isCaseSensitive());
        assertEquals("D", misspellingList.get(0).getSuggestion());

        assertEquals("E", misspellingList.get(1).getWord());
        assertTrue(misspellingList.get(1).isCaseSensitive());
        assertEquals("F", misspellingList.get(1).getSuggestion());

        assertEquals("i", misspellingList.get(2).getWord());
        assertFalse(misspellingList.get(2).isCaseSensitive());
        assertEquals("J", misspellingList.get(2).getSuggestion());
    }

    @Test
    public void testGetWordMisspelling() {
        String wikiText = " conprar||comprar\n madrid|cs|Madrid\n álvaro|cs|Álvaro";
        when(wikipediaService.getArticleContent(anyString())).thenReturn(wikiText);

        misspellingService.updateMisspellingList();

        assertEquals("conprar", misspellingService.getWordMisspelling("conprar").getWord());
        assertEquals("conprar", misspellingService.getWordMisspelling("Conprar").getWord());
        assertEquals("madrid", misspellingService.getWordMisspelling("madrid").getWord());
        assertEquals("álvaro", misspellingService.getWordMisspelling("álvaro").getWord());
        assertNull(misspellingService.getWordMisspelling("Madrid"));
        assertNull(misspellingService.getWordMisspelling("Álvaro"));
    }

}
