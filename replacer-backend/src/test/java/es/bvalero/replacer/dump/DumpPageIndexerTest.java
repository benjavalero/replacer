package es.bvalero.replacer.dump;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.page.index.PageIndexStatus;
import es.bvalero.replacer.page.index.PageIndexer;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DumpPageIndexerTest {

    private final DumpPage dumpPage = DumpPage
        .builder()
        .lang(WikipediaLanguage.getDefault())
        .id(1)
        .namespace(WikipediaNamespace.getDefault())
        .title("T")
        .content("C")
        .lastUpdate(LocalDateTime.now())
        .build();

    @Mock
    private PageIndexer pageIndexer;

    @InjectMocks
    private DumpPageIndexer dumpPageIndexer;

    @BeforeEach
    public void setUp() {
        dumpPageIndexer = new DumpPageIndexer();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testEmptyPageIndexResult() {
        // There is no need to mock the rest of calls
        // The DB page is null as we are not mocking the response from the findByPageId
        when(pageIndexer.indexPageReplacementsInBatch(any(WikipediaPage.class)))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_NOT_INDEXED, Collections.emptyList()));

        PageIndexStatus result = dumpPageIndexer.index(dumpPage);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXED, result);

        verify(pageIndexer).indexPageReplacementsInBatch(any(WikipediaPage.class));
    }

    @Test
    void testIndexNewPageWithReplacements() {
        // No need in this test to build the index result as it would be in the reality with the replacements
        when(pageIndexer.indexPageReplacementsInBatch(any(WikipediaPage.class)))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_INDEXED, Collections.emptyList()));

        PageIndexStatus result = dumpPageIndexer.index(dumpPage);

        assertEquals(PageIndexStatus.PAGE_INDEXED, result);

        verify(pageIndexer).indexPageReplacementsInBatch(any(WikipediaPage.class));
    }

    @Test
    void testPageNotIndexable() {
        when(pageIndexer.indexPageReplacementsInBatch(any(WikipediaPage.class)))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_NOT_INDEXABLE, Collections.emptyList()));

        PageIndexStatus result = dumpPageIndexer.index(dumpPage);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXABLE, result);

        verify(pageIndexer).indexPageReplacementsInBatch(any(WikipediaPage.class));
    }

    @Test
    void testPageNotIndexedByTimestamp() {
        when(pageIndexer.indexPageReplacementsInBatch(any(WikipediaPage.class)))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_NOT_INDEXED, Collections.emptyList()));

        PageIndexStatus result = dumpPageIndexer.index(dumpPage);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXED, result);

        verify(pageIndexer).indexPageReplacementsInBatch(any(WikipediaPage.class));
    }

    @Test
    void testFinish() {
        dumpPageIndexer.finish();

        verify(pageIndexer).forceSave();
    }
}
