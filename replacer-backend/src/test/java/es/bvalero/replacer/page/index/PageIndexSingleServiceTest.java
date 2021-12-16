package es.bvalero.replacer.page.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.page.removeobsolete.RemoveObsoletePageService;
import es.bvalero.replacer.repository.PageIndexRepository;
import es.bvalero.replacer.repository.PageModel;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageIndexSingleServiceTest {

    @Mock
    private PageIndexRepository pageIndexRepository;

    @Mock
    private RemoveObsoletePageService removeObsoletePageService;

    @Mock
    private PageIndexValidator pageIndexValidator;

    @Mock
    private ReplacementFinderService replacementFinderService;

    @Mock
    private IndexablePageComparator indexablePageComparator;

    @Mock
    private PageIndexResultSaver pageIndexResultSaver;

    @InjectMocks
    private PageIndexSingleService pageIndexSingleService;

    private final WikipediaPage page = WikipediaPage
        .builder()
        .id(WikipediaPageId.of(WikipediaLanguage.getDefault(), 1))
        .namespace(WikipediaNamespace.ARTICLE)
        .title("T")
        .content("")
        .lastUpdate(LocalDateTime.now())
        .build();

    @BeforeEach
    void setUp() {
        pageIndexSingleService = new PageIndexSingleService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPageNotIndexable() {
        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(false);

        PageIndexResult result = pageIndexSingleService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXABLE, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator, never()).isIndexableByTimestamp(page, null);
        verify(pageIndexValidator, never()).isIndexableByPageTitle(page, null);
        verify(replacementFinderService, never()).find(any(FinderPage.class));
    }

    @Test
    void testPageNotIndexedByTimestampButIndexedByTitle() {
        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(true);
        when(pageIndexValidator.isIndexableByTimestamp(page, null)).thenReturn(false);
        when(pageIndexValidator.isIndexableByPageTitle(page, null)).thenReturn(true);

        PageIndexResult mockResult = PageIndexResult.builder().status(PageIndexStatus.PAGE_INDEXED).build();
        when(indexablePageComparator.indexPageReplacements(any(IndexablePage.class), isNull())).thenReturn(mockResult);

        PageIndexResult result = pageIndexSingleService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_INDEXED, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator).isIndexableByTimestamp(page, null);
        verify(pageIndexValidator).isIndexableByPageTitle(page, null);
        verify(replacementFinderService).find(any(FinderPage.class));
        verify(indexablePageComparator).indexPageReplacements(any(IndexablePage.class), isNull());
    }

    @Test
    void testPageNotIndexedByTitleButIndexedByTimestamp() {
        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(true);
        when(pageIndexValidator.isIndexableByTimestamp(page, null)).thenReturn(true);
        when(pageIndexValidator.isIndexableByPageTitle(page, null)).thenReturn(false);

        PageIndexResult mockResult = PageIndexResult.builder().status(PageIndexStatus.PAGE_INDEXED).build();
        when(indexablePageComparator.indexPageReplacements(any(IndexablePage.class), isNull())).thenReturn(mockResult);

        PageIndexResult result = pageIndexSingleService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_INDEXED, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator).isIndexableByTimestamp(page, null);
        verify(pageIndexValidator, never()).isIndexableByPageTitle(page, null);
        verify(replacementFinderService).find(any(FinderPage.class));
        verify(indexablePageComparator).indexPageReplacements(any(IndexablePage.class), isNull());
    }

    @Test
    void testPageNotIndexedByTitleOrTimestamp() {
        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(true);
        when(pageIndexValidator.isIndexableByTimestamp(page, null)).thenReturn(false);
        when(pageIndexValidator.isIndexableByPageTitle(page, null)).thenReturn(false);

        PageIndexResult result = pageIndexSingleService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXED, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator).isIndexableByTimestamp(page, null);
        verify(pageIndexValidator).isIndexableByPageTitle(page, null);
        verify(replacementFinderService, never()).find(any(FinderPage.class));
        verify(indexablePageComparator, never())
            .indexPageReplacements(any(IndexablePage.class), any(IndexablePage.class));
    }

    @Test
    void testObsoletePageNotIndexable() {
        PageModel pageModel = PageModel
            .builder()
            .lang(page.getId().getLang().getCode())
            .pageId(page.getId().getPageId())
            .replacements(Collections.emptyList())
            .build();
        when(pageIndexRepository.findPageById(page.getId())).thenReturn(Optional.of(pageModel));

        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(false);

        PageIndexResult result = pageIndexSingleService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXABLE, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator, never()).isIndexableByTimestamp(page, null);
        verify(pageIndexValidator, never()).isIndexableByPageTitle(page, null);
        verify(replacementFinderService, never()).find(any(FinderPage.class));
        verify(pageIndexResultSaver, never()).save(any(PageIndexResult.class));
        verify(removeObsoletePageService).removeObsoletePages(anyCollection());
    }
}
