package es.bvalero.replacer.page.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageRepository;
import es.bvalero.replacer.page.find.WikipediaNamespace;
import es.bvalero.replacer.page.find.WikipediaPage;
import es.bvalero.replacer.page.find.WikipediaTimestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageIndexServiceTest {

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
    private PageRepository pageRepository;
    private PageIndexValidator pageIndexValidator;
    private ReplacementFindService replacementFindService;
    private PageComparator pageComparator;
    private PageComparatorSaver pageComparatorSaver;

    private PageIndexService pageIndexService;

    @BeforeEach
    void setUp() {
        pageRepository = mock(PageRepository.class);
        pageIndexValidator = mock(PageIndexValidator.class);
        replacementFindService = mock(ReplacementFindService.class);
        pageComparator = mock(PageComparator.class);
        pageComparatorSaver = mock(PageComparatorSaver.class);
        pageIndexService =
            new PageIndexService(
                pageRepository,
                pageIndexValidator,
                replacementFindService,
                pageComparator,
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
            .lastUpdate(WikipediaTimestamp.now())
            .queryTimestamp(WikipediaTimestamp.now())
            .redirect(true)
            .build();

        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(true);

        PageIndexResult result = pageIndexService.indexPage(page);

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
            .replacements(List.of())
            .lastUpdate(LocalDate.now())
            .build();
        when(pageRepository.findByKey(page.getPageKey())).thenReturn(Optional.of(dbPage));

        when(pageIndexValidator.isPageIndexableByNamespace(page)).thenReturn(false);

        PageIndexResult result = pageIndexService.indexPage(page);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXABLE, result.getStatus());

        verify(pageIndexValidator).isPageIndexableByNamespace(page);
        verify(pageIndexValidator, never()).isIndexableByTimestamp(page, null);
        verify(replacementFindService, never()).findReplacements(any(WikipediaPage.class));
        verify(pageComparatorSaver, never()).save(any(PageComparatorResult.class));
        verify(pageRepository).removeByKey(anyCollection());
    }
}
