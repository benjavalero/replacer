package es.bvalero.replacer.page.index;

import static es.bvalero.replacer.page.index.PageComparator.toIndexedPage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.find.WikipediaNamespace;
import es.bvalero.replacer.page.find.WikipediaPage;
import es.bvalero.replacer.page.find.WikipediaTimestamp;
import es.bvalero.replacer.page.save.IndexedPageStatus;
import es.bvalero.replacer.page.save.PageSaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageIndexBatchServiceTest {

    private final WikipediaPage page = WikipediaPage.builder()
        .pageKey(PageKey.of(WikipediaLanguage.getDefault(), 1))
        .namespace(WikipediaNamespace.ARTICLE)
        .title("T")
        .content("")
        .lastUpdate(WikipediaTimestamp.now())
        .queryTimestamp(WikipediaTimestamp.now())
        .build();

    // Dependency injection
    private PageSaveRepository pageSaveRepository;
    private PageIndexValidator pageIndexValidator;
    private ReplacementFindService replacementFindService;
    private PageComparator pageComparator;
    private PageBatchService pageBatchService;
    private PageComparatorSaver pageComparatorSaver;

    private PageIndexBatchService pageIndexBatchService;

    @BeforeEach
    void setUp() {
        pageSaveRepository = mock(PageSaveRepository.class);
        pageIndexValidator = mock(PageIndexValidator.class);
        replacementFindService = mock(ReplacementFindService.class);
        pageComparator = mock(PageComparator.class);
        pageBatchService = mock(PageBatchService.class);
        pageComparatorSaver = mock(PageComparatorSaver.class);
        pageIndexBatchService = new PageIndexBatchService(
            pageSaveRepository,
            pageIndexValidator,
            replacementFindService,
            pageComparator,
            pageBatchService,
            pageComparatorSaver
        );
    }

    @Test
    void testPageIndexedByTimestamp() {
        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(true);
        when(pageIndexValidator.isIndexableByTimestamp(page, null)).thenReturn(true);

        IndexedPage indexedPage = IndexedPage.builder()
            .pageKey(page.getPageKey())
            .title(page.getTitle())
            .lastUpdate(page.getLastUpdate().toLocalDate())
            .build();
        IndexedPage comparatorResult = toIndexedPage(page, IndexedPageStatus.ADD); // Any change
        when(pageComparator.indexPageReplacements(any(IndexablePage.class), anyCollection(), isNull())).thenReturn(
            comparatorResult
        );

        PageIndexResult result = pageIndexBatchService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_INDEXED, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator).isIndexableByTimestamp(page, null);
        verify(replacementFindService).findReplacements(page.toFinderPage());
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
        verify(replacementFindService, never()).findReplacements(any(FinderPage.class));
        verify(pageComparator, never()).indexPageReplacements(
            any(IndexablePage.class),
            anyCollection(),
            any(IndexedPage.class)
        );
    }
}
