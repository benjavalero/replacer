package es.bvalero.replacer.page.index;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.domain.WikipediaPage;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { PageIndexValidator.class })
class PageIndexValidatorTest {

    @Autowired
    private PageIndexValidator pageIndexValidator;

    @Test
    void testIsPageIndexableByNamespace() {
        WikipediaPage notIndexable = mock(WikipediaPage.class);
        when(notIndexable.getNamespace()).thenReturn(WikipediaNamespace.WIKIPEDIA);
        WikipediaPage articlePage = mock(WikipediaPage.class);
        when(articlePage.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        WikipediaPage annexPage = mock(WikipediaPage.class);
        when(annexPage.getNamespace()).thenReturn(WikipediaNamespace.ANNEX);

        assertFalse(pageIndexValidator.isPageIndexableByNamespace(notIndexable));
        assertTrue(pageIndexValidator.isPageIndexableByNamespace(articlePage));
        assertTrue(pageIndexValidator.isPageIndexableByNamespace(annexPage));
    }

    @Test
    void testIsPageNotIndexableByTitle() {
        WikipediaPage page1 = mock(WikipediaPage.class);
        when(page1.getTitle()).thenReturn("T");
        assertTrue(pageIndexValidator.isIndexableByPageTitle(page1, null));

        WikipediaPage page2 = mock(WikipediaPage.class);
        when(page2.getTitle()).thenReturn("T");
        IndexablePage dbPage2 = mock(IndexablePage.class);
        when(dbPage2.getTitle()).thenReturn("Z");
        assertTrue(pageIndexValidator.isIndexableByPageTitle(page2, dbPage2));

        WikipediaPage page3 = mock(WikipediaPage.class);
        when(page3.getTitle()).thenReturn("T");
        IndexablePage dbPage3 = mock(IndexablePage.class);
        when(dbPage3.getTitle()).thenReturn("T");
        assertFalse(pageIndexValidator.isIndexableByPageTitle(page3, dbPage3));
    }

    @Test
    void testIsPageNotIndexableByTimestamp() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1);

        WikipediaPage page1 = mock(WikipediaPage.class);
        when(page1.getLastUpdate()).thenReturn(today);
        assertTrue(pageIndexValidator.isIndexableByTimestamp(page1, null));

        WikipediaPage page2 = mock(WikipediaPage.class);
        when(page2.getLastUpdate()).thenReturn(today);
        IndexablePage dbPage2 = mock(IndexablePage.class);
        when(dbPage2.getLastUpdate()).thenReturn(today.toLocalDate());
        assertTrue(pageIndexValidator.isIndexableByTimestamp(page2, dbPage2));

        WikipediaPage page3 = mock(WikipediaPage.class);
        when(page3.getLastUpdate()).thenReturn(yesterday);
        IndexablePage dbPage3 = mock(IndexablePage.class);
        when(dbPage3.getLastUpdate()).thenReturn(today.toLocalDate());
        assertFalse(pageIndexValidator.isIndexableByTimestamp(page3, dbPage3));

        WikipediaPage page4 = mock(WikipediaPage.class);
        when(page4.getLastUpdate()).thenReturn(today);
        IndexablePage dbPage4 = mock(IndexablePage.class);
        when(dbPage4.getLastUpdate()).thenReturn(yesterday.toLocalDate());
        assertTrue(pageIndexValidator.isIndexableByTimestamp(page4, dbPage4));
    }
}
