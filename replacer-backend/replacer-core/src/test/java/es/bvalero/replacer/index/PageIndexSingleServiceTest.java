package es.bvalero.replacer.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageService;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.time.LocalDate;
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
    private PageService pageService;

    @Mock
    private PageIndexValidator pageIndexValidator;

    @Mock
    private ReplacementFindService replacementFindService;

    @Mock
    private PageComparator pageComparator;

    @Mock
    private PageComparatorSaver pageComparatorSaver;

    @InjectMocks
    private PageIndexSingleService pageIndexSingleService;

    private final WikipediaPage page = WikipediaPage
        .builder()
        .pageKey(PageKey.of(WikipediaLanguage.getDefault(), 1))
        .namespace(WikipediaNamespace.ARTICLE)
        .title("T")
        .content("")
        .lastUpdate(LocalDateTime.now())
        .queryTimestamp(LocalDateTime.now())
        .build();

    @BeforeEach
    void setUp() {
        pageIndexSingleService = new PageIndexSingleService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPageNotIndexableByNamespace() {
        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(false);

        PageIndexResult result = pageIndexSingleService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXABLE, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator, never()).isIndexableByTimestamp(page, null);
        verify(replacementFindService, never()).findReplacements(any(WikipediaPage.class));
    }

    @Test
    void testPageNotIndexableByRedirection() {
        final WikipediaPage page = WikipediaPage
            .builder()
            .pageKey(PageKey.of(WikipediaLanguage.getDefault(), 1))
            .namespace(WikipediaNamespace.ARTICLE)
            .title("T")
            .content("")
            .lastUpdate(LocalDateTime.now())
            .queryTimestamp(LocalDateTime.now())
            .redirect(true)
            .build();

        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(true);

        PageIndexResult result = pageIndexSingleService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXABLE, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator, never()).isIndexableByTimestamp(page, null);
        verify(replacementFindService, never()).findReplacements(any(WikipediaPage.class));
    }

    @Test
    void testObsoletePageNotIndexable() {
        IndexedPage dbPage = IndexedPage
            .builder()
            .pageKey(page.getPageKey())
            .title("T")
            .replacements(Collections.emptyList())
            .lastUpdate(LocalDate.now())
            .build();
        when(pageService.findPageByKey(page.getPageKey())).thenReturn(Optional.of(dbPage));

        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(false);

        PageIndexResult result = pageIndexSingleService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXABLE, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator, never()).isIndexableByTimestamp(page, null);
        verify(replacementFindService, never()).findReplacements(any(WikipediaPage.class));
        verify(pageComparatorSaver, never()).save(any(PageComparatorResult.class));
        verify(pageService).removePagesByKey(anyCollection());
    }
}