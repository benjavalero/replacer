package es.bvalero.replacer.page.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.page.findreplacement.PageReplacementFinder;
import es.bvalero.replacer.page.removeobsolete.RemoveObsoletePageService;
import es.bvalero.replacer.repository.PageIndexRepository;
import es.bvalero.replacer.repository.PageModel;
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
    private PageIndexRepository pageIndexRepository;

    @Mock
    private RemoveObsoletePageService removeObsoletePageService;

    @Mock
    private PageIndexValidator pageIndexValidator;

    @Mock
    private PageReplacementFinder pageReplacementFinder;

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
    void testPageNotIndexableByNamespace() {
        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(false);

        PageIndexResult result = pageIndexSingleService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXABLE, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator, never()).isIndexableByTimestamp(page, null);
        verify(pageIndexValidator, never()).isIndexableByPageTitle(page, null);
        verify(pageReplacementFinder, never()).findReplacements(any(WikipediaPage.class));
    }

    @Test
    void testPageNotIndexableByRedirection() {
        final WikipediaPage page = WikipediaPage
            .builder()
            .id(WikipediaPageId.of(WikipediaLanguage.getDefault(), 1))
            .namespace(WikipediaNamespace.ARTICLE)
            .title("T")
            .content("")
            .lastUpdate(LocalDateTime.now())
            .redirect(true)
            .build();

        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(true);

        PageIndexResult result = pageIndexSingleService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXABLE, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator, never()).isIndexableByTimestamp(page, null);
        verify(pageIndexValidator, never()).isIndexableByPageTitle(page, null);
        verify(pageReplacementFinder, never()).findReplacements(any(WikipediaPage.class));
    }

    @Test
    void testObsoletePageNotIndexable() {
        PageModel pageModel = PageModel
            .builder()
            .lang(page.getId().getLang().getCode())
            .pageId(page.getId().getPageId())
            .title("T")
            .replacements(Collections.emptyList())
            .lastUpdate(LocalDate.now())
            .build();
        when(pageIndexRepository.findPageById(page.getId())).thenReturn(Optional.of(pageModel));

        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(false);

        PageIndexResult result = pageIndexSingleService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXABLE, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator, never()).isIndexableByTimestamp(page, null);
        verify(pageIndexValidator, never()).isIndexableByPageTitle(page, null);
        verify(pageReplacementFinder, never()).findReplacements(any(WikipediaPage.class));
        verify(pageIndexResultSaver, never()).save(any(PageIndexResult.class));
        verify(removeObsoletePageService).removeObsoletePages(anyCollection());
    }
}
