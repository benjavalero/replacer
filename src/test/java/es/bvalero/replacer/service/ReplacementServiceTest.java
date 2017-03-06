package es.bvalero.replacer.service;

import es.bvalero.replacer.domain.Misspelling;
import es.bvalero.replacer.domain.Replacement;
import es.bvalero.replacer.domain.ReplacementBD;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ReplacementServiceTest {

    @Mock
    private ReplacementDao replacementDao;

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
    public void testFindAllReviewedReplacements() {
        List<ReplacementBD> repList = new ArrayList<>();
        repList.add(new ReplacementBD("A", null));
        repList.add(new ReplacementBD("B", null));
        repList.add(new ReplacementBD("B", null));
        repList.add(new ReplacementBD("C", null));

        when(replacementDao.findAllReviewedReplacements()).thenReturn(repList);

        Map<String, List<ReplacementBD>> map = replacementService.findAllReviewedReplacements();
        assertEquals(3, map.size());
        assertEquals(1, map.get("A").size());
        assertEquals(2, map.get("B").size());
        assertEquals(1, map.get("C").size());
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
    public void testFindReplacementsForDb() {
        when(misspellingService.getWordMisspelling("A")).thenReturn(new Misspelling("A", true, "Á"));
        when(misspellingService.getWordMisspelling("E")).thenReturn(new Misspelling("E", true, "É"));
        when(misspellingService.getWordMisspelling("I")).thenReturn(new Misspelling("I", true, "Í"));
        when(misspellingService.getWordMisspelling("O")).thenReturn(new Misspelling("O", true, "Ó"));

        String text = "A B E E ''I'' O";
        List<ReplacementBD> list = replacementService.findReplacementsForDB("", text);
        assertEquals(3, list.size());
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
