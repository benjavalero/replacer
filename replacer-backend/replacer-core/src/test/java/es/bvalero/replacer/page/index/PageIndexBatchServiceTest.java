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

class PageIndexBatchServiceTest {

    private final WikipediaPage page = WikipediaPage
        .builder()
        .pageKey(PageKey.of(WikipediaLanguage.getDefault(), 1))
        .namespace(WikipediaNamespace.ARTICLE)
        .title("T")
        .content("")
        .lastUpdate(WikipediaTimestamp.now())
        .queryTimestamp(WikipediaTimestamp.now())
        .build();

    // Dependency injection
    private PageBatchService pageBatchService;
    private PageIndexValidator pageIndexValidator;
    private ReplacementFindService replacementFindService;
    private PageComparator pageComparator;
    private PageComparatorSaver pageComparatorSaver;

    private PageIndexBatchService pageIndexBatchService;

    @BeforeEach
    void setUp() {
        pageBatchService = mock(PageBatchService.class);
        pageIndexValidator = mock(PageIndexValidator.class);
        replacementFindService = mock(ReplacementFindService.class);
        pageComparator = mock(PageComparator.class);
        pageComparatorSaver = mock(PageComparatorSaver.class);
        pageIndexBatchService =
            new PageIndexBatchService(
                pageBatchService,
                pageIndexValidator,
                replacementFindService,
                pageComparator,
                pageComparatorSaver
            );
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
        comparatorResult.addPageToCreate(indexedPage); // Any change
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
