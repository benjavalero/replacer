package es.bvalero.replacer.parse;

import es.bvalero.replacer.domain.ReplacementBD;
import es.bvalero.replacer.service.ReplacementService;
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
    private ReplacementService replacementService;

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

        ReplacementBD replacementBD = new ReplacementBD();
        replacementBD.setTitle(articleTitle);
        replacementBD.setLastReviewed(new Timestamp(today.getTimeInMillis()));
        List<ReplacementBD> replacementList = new ArrayList<>();
        replacementList.add(replacementBD);
        Map<String, List<ReplacementBD>> replacementMap = new HashMap<>();
        replacementMap.put(articleTitle, replacementList);
        Mockito.when(replacementService.findAllReviewedReplacements()).thenReturn(replacementMap);

        Date articleTimestamp = yesterday.getTime();

        Assert.assertTrue(handler.isReviewedAfter(articleTitle, articleTimestamp));

        handler.processArticle(null, articleTitle, articleTimestamp);

        Mockito.verify(replacementService, Mockito.times(0))
                .insertReplacements(Mockito.anyListOf(ReplacementBD.class));
    }

    @Test
    public void testProcess() {
        Assert.assertFalse(handler.isReviewedAfter(null, null));

        handler.processArticle(null, null, null);

        Mockito.verify(replacementService).insertReplacements(Mockito.anyListOf(ReplacementBD.class));
        Mockito.verify(replacementService).deleteReplacementsByTitle(Mockito.anyString());
    }

}
