package es.bvalero.replacer.service;

import es.bvalero.replacer.domain.Misspelling;
import es.bvalero.replacer.domain.Replacement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ReplacementServiceTest {

    @Mock
    private MisspellingService misspellingService;

    @InjectMocks
    private ReplacementService replacementService;

    @Before
    public void setUp() {
        replacementService = new ReplacementService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testReplacementRevertSort() {
        List<Replacement> repList = new ArrayList<>();
        repList.add(new Replacement(1, null));
        repList.add(new Replacement(2, null));
        repList.add(new Replacement(2, null));
        repList.add(new Replacement(3, null));

        Collections.sort(repList);
        assertEquals(4, repList.size());
        assertEquals(Integer.valueOf(3), repList.get(0).getPosition());
        assertEquals(Integer.valueOf(2), repList.get(1).getPosition());
        assertEquals(Integer.valueOf(2), repList.get(2).getPosition());
        assertEquals(Integer.valueOf(1), repList.get(3).getPosition());
    }

    @Test
    public void testFindReplacements() {
        when(misspellingService.getWordMisspelling("A")).thenReturn(new Misspelling("A", true, "Á"));
        when(misspellingService.getWordMisspelling("E")).thenReturn(new Misspelling("E", true, "É"));
        when(misspellingService.getWordMisspelling("I")).thenReturn(new Misspelling("I", true, "Í"));
        when(misspellingService.getWordMisspelling("O")).thenReturn(new Misspelling("O", true, "Ó"));

        String text = "A B E E ''I'' O";
        List<Replacement> list = replacementService.findReplacements(text);
        assertEquals(4, list.size());
    }

}
