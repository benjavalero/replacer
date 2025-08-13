package es.bvalero.replacer.page.index;

import static es.bvalero.replacer.page.index.PageComparator.toIndexedPage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.ReplacementFindApi;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.save.IndexedPageStatus;
import es.bvalero.replacer.page.save.PageSaveRepository;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageIndexBatchServiceTest {

    private final IndexablePage page = IndexablePage.builder()
        .pageKey(PageKey.of(WikipediaLanguage.getDefault(), 1))
        .namespace(WikipediaNamespace.getDefault().getValue())
        .title("T")
        .content("")
        .lastUpdate(WikipediaTimestamp.now().toString())
        .build();

    // Dependency injection
    private PageSaveRepository pageSaveRepository;
    private PageIndexValidator pageIndexValidator;
    private ReplacementFindApi replacementFindApi;
    private PageComparator pageComparator;
    private PageBatchService pageBatchService;
    private PageComparatorSaver pageComparatorSaver;

    private PageIndexBatchService pageIndexBatchService;

    @BeforeEach
    void setUp() {
        pageSaveRepository = mock(PageSaveRepository.class);
        pageIndexValidator = mock(PageIndexValidator.class);
        replacementFindApi = mock(ReplacementFindApi.class);
        pageComparator = mock(PageComparator.class);
        pageBatchService = mock(PageBatchService.class);
        pageComparatorSaver = mock(PageComparatorSaver.class);
        pageIndexBatchService = new PageIndexBatchService(
            pageSaveRepository,
            pageIndexValidator,
            replacementFindApi,
            pageComparator,
            pageBatchService,
            pageComparatorSaver
        );
    }

    @Test
    void testPageIndexedByTimestamp() {
        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(true);
        when(pageIndexValidator.isIndexableByTimestamp(page, null)).thenReturn(true);

        IndexedPage comparatorResult = toIndexedPage(page, IndexedPageStatus.ADD); // Any change
        when(pageComparator.indexPageReplacements(any(IndexablePage.class), anyCollection(), isNull())).thenReturn(
            comparatorResult
        );

        PageIndexResult result = pageIndexBatchService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_INDEXED, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator).isIndexableByTimestamp(page, null);
        verify(replacementFindApi).findReplacements(page.toFinderPage());
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
        verify(replacementFindApi, never()).findReplacements(any(FinderPage.class));
        verify(pageComparator, never()).indexPageReplacements(
            any(IndexablePage.class),
            anyCollection(),
            any(IndexedPage.class)
        );
    }
}
