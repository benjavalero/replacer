package es.bvalero.replacer.dump;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.page.index.IndexablePage;
import es.bvalero.replacer.page.index.PageIndexer;
import es.bvalero.replacer.page.repository.PageModel;
import es.bvalero.replacer.page.repository.PageRepository;
import es.bvalero.replacer.page.repository.ReplacementModel;
import es.bvalero.replacer.page.validate.PageValidator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DumpPageProcessorTest {

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
    private PageValidator pageValidator;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private PageIndexer pageIndexer;

    @InjectMocks
    private DumpPageProcessor dumpPageProcessor;

    @BeforeEach
    public void setUp() {
        dumpPageProcessor = new DumpPageProcessor();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testEmptyPageIndexResult() throws ReplacerException {
        // There is no need to mock the rest of calls
        // The DB page is null as we are not mocking the response from the findByPageId
        when(pageIndexer.indexPageReplacements(any(WikipediaPage.class), isNull())).thenReturn(false);

        DumpPageProcessorResult result = dumpPageProcessor.process(dumpPage);

        assertEquals(DumpPageProcessorResult.PAGE_NOT_PROCESSED, result);

        verify(pageRepository).findByPageId(dumpPageId);
        verify(pageValidator).validateProcessable(any(WikipediaPage.class));
        verify(pageIndexer).indexPageReplacements(any(WikipediaPage.class), isNull());
    }

    @Test
    void testProcessNewPageWithReplacements() throws ReplacerException {
        when(pageRepository.findByPageId(dumpPageId)).thenReturn(Optional.empty());

        // No need in this test to build the index result as it would be in the reality with the replacements
        when(pageIndexer.indexPageReplacements(any(WikipediaPage.class), isNull())).thenReturn(true);

        DumpPageProcessorResult result = dumpPageProcessor.process(dumpPage);

        assertEquals(DumpPageProcessorResult.PAGE_PROCESSED, result);

        verify(pageRepository).findByPageId(dumpPageId);
        verify(pageValidator).validateProcessable(any(WikipediaPage.class));
        verify(pageIndexer).indexPageReplacements(any(WikipediaPage.class), isNull());
    }

    @Test
    void testPageNotProcessable() throws ReplacerException {
        when(pageRepository.findByPageId(dumpPageId)).thenReturn(Optional.empty());

        doThrow(ReplacerException.class).when(pageValidator).validateProcessable(any(WikipediaPage.class));

        DumpPageProcessorResult result = dumpPageProcessor.process(dumpPage);

        assertEquals(DumpPageProcessorResult.PAGE_NOT_PROCESSABLE, result);

        verify(pageRepository).findByPageId(dumpPageId);
        verify(pageValidator).validateProcessable(any(WikipediaPage.class));
        verify(pageIndexer, never()).indexPageReplacements(any(IndexablePage.class), any(IndexablePage.class));
    }

    @Test
    void testPageNotProcessedByTimestamp() throws ReplacerException {
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
            .title(dumpPage.getTitle()) // The title must match so the page is not processed
            .replacements(List.of(dbReplacement))
            .build();
        when(pageRepository.findByPageId(dumpPageId)).thenReturn(Optional.of(dbPage));

        DumpPageProcessorResult result = dumpPageProcessor.process(dumpPage);

        assertEquals(DumpPageProcessorResult.PAGE_NOT_PROCESSED, result);

        verify(pageRepository).findByPageId(dumpPageId);
        verify(pageValidator).validateProcessable(any(WikipediaPage.class));
        verify(pageIndexer, never()).indexPageReplacements(any(IndexablePage.class), any(IndexablePage.class));
    }

    @Test
    void testFinish() {
        dumpPageProcessor.finish();

        verify(pageRepository).resetCache();
        verify(pageIndexer).forceSave();
    }
}
