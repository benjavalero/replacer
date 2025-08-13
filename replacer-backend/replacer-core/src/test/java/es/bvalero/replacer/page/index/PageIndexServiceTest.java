package es.bvalero.replacer.page.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.ReplacementFindApi;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.find.PageRepository;
import es.bvalero.replacer.page.save.PageSaveRepository;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageIndexServiceTest {

    private final IndexablePage page = IndexablePage.builder()
        .pageKey(PageKey.of(WikipediaLanguage.getDefault(), 1))
        .namespace(WikipediaNamespace.getDefault().getValue())
        .title("T")
        .content("")
        .lastUpdate(WikipediaTimestamp.now().toString())
        .build();

    // Dependency injection
    private WikipediaPageRepository wikipediaPageRepository;
    private PageSaveRepository pageSaveRepository;
    private PageIndexValidator pageIndexValidator;
    private ReplacementFindApi replacementFindApi;
    private PageComparator pageComparator;
    private PageRepository pageRepository;
    private PageComparatorSaver pageComparatorSaver;

    private PageIndexService pageIndexService;

    @BeforeEach
    void setUp() {
        wikipediaPageRepository = mock(WikipediaPageRepository.class);
        pageSaveRepository = mock(PageSaveRepository.class);
        pageIndexValidator = mock(PageIndexValidator.class);
        replacementFindApi = mock(ReplacementFindApi.class);
        pageComparator = mock(PageComparator.class);
        pageRepository = mock(PageRepository.class);
        pageComparatorSaver = mock(PageComparatorSaver.class);
        pageIndexService = new PageIndexService(
            wikipediaPageRepository,
            pageSaveRepository,
            pageIndexValidator,
            replacementFindApi,
            pageComparator,
            pageRepository,
            pageComparatorSaver
        );
    }

    @Test
    void testPageNotIndexableByNamespace() {
        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(false);

        PageIndexResult result = pageIndexService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXABLE, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator, never()).isIndexableByTimestamp(page, null);
        verify(replacementFindApi, never()).findReplacements(any(FinderPage.class));
    }

    @Test
    void testPageNotIndexableByRedirection() {
        final IndexablePage page = IndexablePage.builder()
            .pageKey(PageKey.of(WikipediaLanguage.getDefault(), 1))
            .namespace(WikipediaNamespace.getDefault().getValue())
            .title("T")
            .content("")
            .lastUpdate(WikipediaTimestamp.now().toString())
            .redirect(true)
            .build();

        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(true);

        PageIndexResult result = pageIndexService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXABLE, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator, never()).isIndexableByTimestamp(page, null);
        verify(replacementFindApi, never()).findReplacements(any(FinderPage.class));
    }

    @Test
    void testObsoletePageNotIndexable() {
        IndexedPage dbPage = IndexedPage.builder()
            .pageKey(page.getPageKey())
            .title("T")
            .replacements(List.of())
            .lastUpdate(LocalDate.now())
            .build();
        when(pageRepository.findByKey(page.getPageKey())).thenReturn(Optional.of(dbPage));

        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(false);

        PageIndexResult result = pageIndexService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXABLE, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator, never()).isIndexableByTimestamp(page, null);
        verify(replacementFindApi, never()).findReplacements(any(FinderPage.class));
        verify(pageComparatorSaver, never()).save(any(IndexedPage.class));
        verify(pageSaveRepository).removeByKey(anyCollection());
    }
}
