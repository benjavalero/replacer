package es.bvalero.replacer.page.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageBatchService;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageIndexBatchServiceTest {

    @Mock
    private PageBatchService pageBatchService;

    @Mock
    private PageIndexValidator pageIndexValidator;

    @Mock
    private ReplacementFindService replacementFindService;

    @Mock
    private PageComparator pageComparator;

    @Mock
    private PageComparatorSaver pageComparatorSaver;

    @InjectMocks
    private PageIndexBatchService pageIndexBatchService;

    private final WikipediaPage page = WikipediaPage
        .builder()
        .pageKey(PageKey.of(WikipediaLanguage.getDefault(), 1))
        .namespace(WikipediaNamespace.ARTICLE)
        .title("T")
        .content("")
        .lastUpdate(WikipediaTimestamp.now())
        .queryTimestamp(WikipediaTimestamp.now())
        .build();

    @BeforeEach
    void setUp() {
        pageIndexBatchService = new PageIndexBatchService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPageIndexedByTimestamp() {
        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(true);
        when(pageIndexValidator.isIndexableByTimestamp(page, null)).thenReturn(true);

        PageComparatorResult comparatorResult = PageComparatorResult.of(page.getPageKey().getLang());
        IndexedPage indexedPage = IndexedPage
            .builder()
            .pageKey(page.getPageKey())
            .title(page.getTitle())
            .lastUpdate(page.getLastUpdate().toLocalDate())
            .build();
        comparatorResult.addPage(indexedPage); // Any change
        when(pageComparator.indexPageReplacements(any(IndexablePage.class), anyCollection(), isNull()))
            .thenReturn(comparatorResult);

        PageIndexResult result = pageIndexBatchService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_INDEXED, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator).isIndexableByTimestamp(page, null);
        verify(replacementFindService).findReplacements(page);
        verify(pageComparator).indexPageReplacements(any(IndexablePage.class), anyCollection(), isNull());
    }

    @Test
    void testPageNotIndexedByTimestamp() {
        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(true);
        when(pageIndexValidator.isIndexableByTimestamp(page, null)).thenReturn(false);

        PageIndexResult result = pageIndexBatchService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXED, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator).isIndexableByTimestamp(page, null);
        verify(replacementFindService, never()).findReplacements(any(WikipediaPage.class));
        verify(pageComparator, never())
            .indexPageReplacements(any(IndexablePage.class), anyCollection(), any(IndexedPage.class));
    }
}
