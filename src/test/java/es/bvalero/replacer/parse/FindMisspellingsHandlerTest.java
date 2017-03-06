package es.bvalero.replacer.parse;

import es.bvalero.replacer.domain.ReplacementBD;
import es.bvalero.replacer.service.MisspellingService;
import es.bvalero.replacer.service.ReplacementService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class FindMisspellingsHandlerTest {

    @Mock
    private MisspellingService misspellingService;

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
        handler.setCurrentText("");
        handler.setIncremental(Boolean.TRUE);
        GregorianCalendar oldCal = new GregorianCalendar();
        oldCal.add(GregorianCalendar.DATE, -50);
        handler.setCurrentTimestamp(oldCal.getTime());

        assertTrue(handler.isIncremental());
        assertFalse(handler.isReviewed());
        assertFalse(handler.isRecentlyUpdated());

        handler.processArticle();

        verify(replacementService, times(0)).insertReplacements(anyListOf(ReplacementBD.class));
    }

    @Test
    public void testProcessOnlyNew() {
        handler.setCurrentTitle("X");
        Map<String, List<ReplacementBD>> reviewed = new HashMap<>();
        reviewed.put("X", new ArrayList<ReplacementBD>());
        when(replacementService.findAllReviewedReplacements()).thenReturn(reviewed);

        handler.setCurrentText("");
        handler.setIncremental(Boolean.FALSE);
        GregorianCalendar cal = new GregorianCalendar();
        handler.setCurrentTimestamp(cal.getTime());

        assertFalse(handler.isIncremental());
        assertTrue(handler.isReviewed());
        assertTrue(handler.isRecentlyUpdated());

        handler.processArticle();

        verify(replacementService).insertReplacements(anyListOf(ReplacementBD.class));
        verify(replacementService, times(0)).deleteReplacementsByTitle(anyString());
    }

    @Test
    public void testProcessAll() {
        handler.setCurrentText("");
        handler.setIncremental(Boolean.TRUE);
        GregorianCalendar cal = new GregorianCalendar();
        handler.setCurrentTimestamp(cal.getTime());

        assertTrue(handler.isIncremental());
        assertFalse(handler.isReviewed());
        assertTrue(handler.isRecentlyUpdated());

        handler.processArticle();

        verify(replacementService).insertReplacements(anyListOf(ReplacementBD.class));
        verify(replacementService).deleteReplacementsByTitle(anyString());
    }

    @Test
    public void testIsRecentlyUpdated() {
        // Updated today
        GregorianCalendar cal = new GregorianCalendar();
        handler.setCurrentTimestamp(cal.getTime());
        assertTrue(handler.isRecentlyUpdated());

        // Today - 29
        cal.add(GregorianCalendar.DATE, -29);
        handler.setCurrentTimestamp(cal.getTime());
        assertTrue(handler.isRecentlyUpdated());

        // Today - 30
        cal.add(GregorianCalendar.DATE, -1);
        handler.setCurrentTimestamp(cal.getTime());
        assertFalse(handler.isRecentlyUpdated());

        // Today - 40
        cal.add(GregorianCalendar.DATE, -10);
        handler.setCurrentTimestamp(cal.getTime());
        assertFalse(handler.isRecentlyUpdated());
    }

}
