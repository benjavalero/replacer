package es.bvalero.replacer.dump;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.page.index.IndexablePage;
import es.bvalero.replacer.page.index.NonIndexablePageException;
import es.bvalero.replacer.page.index.PageIndexStatus;
import es.bvalero.replacer.page.index.PageIndexer;
import es.bvalero.replacer.page.repository.PageModel;
import es.bvalero.replacer.page.repository.PageRepository;
import es.bvalero.replacer.page.repository.ReplacementModel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    private final WikipediaPageId dumpPageId = WikipediaPageId.of(dumpPage.getLang(), dumpPage.getId());

    @Mock
    private PageRepository pageRepository;

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
    void testEmptyPageIndexResult() throws NonIndexablePageException {
        // There is no need to mock the rest of calls
        // The DB page is null as we are not mocking the response from the findByPageId
        when(pageIndexer.indexPageReplacements(any(WikipediaPage.class), isNull(IndexablePage.class)))
            .thenReturn(false);

        PageIndexStatus result = dumpPageIndexer.index(dumpPage);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXED, result);

        verify(pageRepository).findByPageId(dumpPageId);
        verify(pageIndexer).indexPageReplacements(any(WikipediaPage.class), isNull(IndexablePage.class));
    }

    @Test
    void testIndexNewPageWithReplacements() throws NonIndexablePageException {
        when(pageRepository.findByPageId(dumpPageId)).thenReturn(Optional.empty());

        // No need in this test to build the index result as it would be in the reality with the replacements
        when(pageIndexer.indexPageReplacements(any(WikipediaPage.class), isNull(IndexablePage.class))).thenReturn(true);

        PageIndexStatus result = dumpPageIndexer.index(dumpPage);

        assertEquals(PageIndexStatus.PAGE_INDEXED, result);

        verify(pageRepository).findByPageId(dumpPageId);
        verify(pageIndexer).indexPageReplacements(any(WikipediaPage.class), isNull(IndexablePage.class));
    }

    @Test
    void testPageNotIndexable() throws NonIndexablePageException {
        when(pageRepository.findByPageId(dumpPageId)).thenReturn(Optional.empty());

        doThrow(NonIndexablePageException.class)
            .when(pageIndexer)
            .indexPageReplacements(any(WikipediaPage.class), isNull(IndexablePage.class));

        PageIndexStatus result = dumpPageIndexer.index(dumpPage);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXABLE, result);

        verify(pageRepository).findByPageId(dumpPageId);
        verify(pageIndexer).indexPageReplacements(any(WikipediaPage.class), isNull(IndexablePage.class));
    }

    @Test
    void testPageNotIndexedByTimestamp() throws NonIndexablePageException {
        // The page exists in DB but has been updated after the dump
        LocalDate updated = dumpPage.getLastUpdate().toLocalDate().plusDays(1);
        ReplacementModel dbReplacement = ReplacementModel
            .builder()
            .lang(dumpPageId.getLang())
            .pageId(dumpPageId.getPageId())
            .type("")
            .subtype("")
            .position(0)
            .context("")
            .lastUpdate(updated)
            .build();
        PageModel dbPage = PageModel
            .builder()
            .lang(dumpPageId.getLang())
            .pageId(dumpPageId.getPageId())
            .title(dumpPage.getTitle()) // The title must match so the page is not indexed
            .replacements(List.of(dbReplacement))
            .build();
        when(pageRepository.findByPageId(dumpPageId)).thenReturn(Optional.of(dbPage));

        PageIndexStatus result = dumpPageIndexer.index(dumpPage);

        assertEquals(PageIndexStatus.PAGE_NOT_INDEXED, result);

        verify(pageRepository).findByPageId(dumpPageId);
        verify(pageIndexer).indexPageReplacements(any(WikipediaPage.class), any(IndexablePage.class));
    }

    @Test
    void testFinish() {
        dumpPageIndexer.finish();

        verify(pageRepository).resetCache();
        verify(pageIndexer).forceSave();
    }
}
