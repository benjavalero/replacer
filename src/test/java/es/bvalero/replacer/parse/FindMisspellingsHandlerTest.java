package es.bvalero.replacer.parse;

import es.bvalero.replacer.domain.Misspelling;
import es.bvalero.replacer.persistence.ReplacementDao;
import es.bvalero.replacer.persistence.pojo.ReplacementDb;
import es.bvalero.replacer.service.MisspellingService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;
import java.util.*;

public class FindMisspellingsHandlerTest {

    @Mock
    private ReplacementDao replacementDao;

    @Mock
    private MisspellingService misspellingService;

    @InjectMocks
    private FindMisspellingsHandler handler;

    @Before
    public void setUp() {
        handler = new FindMisspellingsHandler();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testNotProcess() {
        String articleTitle = "A";

        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);

        ReplacementDb replacementDb = new ReplacementDb();
        replacementDb.setTitle(articleTitle);
        replacementDb.setLastReviewed(new Timestamp(today.getTimeInMillis()));
        List<ReplacementDb> replacementList = new ArrayList<>();
        replacementList.add(replacementDb);
        Mockito.when(replacementDao.findAllReviewedReplacements()).thenReturn(replacementList);

        Date articleTimestamp = yesterday.getTime();

        Assert.assertTrue(handler.isReviewedAfter(articleTitle, articleTimestamp));

        handler.processArticle(null, articleTitle, articleTimestamp);

        Mockito.verify(replacementDao, Mockito.times(0))
                .insertAll(Mockito.anyListOf(ReplacementDb.class));
    }

    @Test
    public void testProcess() {
        Assert.assertFalse(handler.isReviewedAfter(null, null));

        handler.processArticle("", null, null);

        Mockito.verify(replacementDao).insertAll(Mockito.anyListOf(ReplacementDb.class));
        Mockito.verify(replacementDao).deleteReplacementsByTitle(Mockito.anyString());
    }

    @Test
    public void testFindWordReplacements() {
        Mockito.when(misspellingService.getWordMisspelling("A")).thenReturn(new Misspelling("A", true, "Á"));
        Mockito.when(misspellingService.getWordMisspelling("E")).thenReturn(new Misspelling("E", true, "É"));
        Mockito.when(misspellingService.getWordMisspelling("I")).thenReturn(new Misspelling("I", true, "Í"));
        Mockito.when(misspellingService.getWordMisspelling("O")).thenReturn(new Misspelling("O", true, "Ó"));

        String text = "A B E E ''I'' O";
        Set<ReplacementDb> replacements = handler.findWordReplacements("", text);
        Assert.assertEquals(3, replacements.size());
    }

}
