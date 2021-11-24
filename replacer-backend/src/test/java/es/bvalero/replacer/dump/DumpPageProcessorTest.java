package es.bvalero.replacer.dump;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import es.bvalero.replacer.page.index.IndexablePage;
import es.bvalero.replacer.page.index.PageIndexHelper;
import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.page.index.PageIndexResultSaver;
import es.bvalero.replacer.page.repository.PageModel;
import es.bvalero.replacer.page.repository.PageRepository;
import es.bvalero.replacer.page.repository.ReplacementModel;
import es.bvalero.replacer.page.validate.PageValidator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private PageIndexResultSaver pageIndexResultSaver;

    @Mock
    private ReplacementFinderService replacementFinderService;

    @Mock
    private PageIndexHelper pageIndexHelper;

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
        when(pageIndexHelper.indexPageReplacements(any(IndexablePage.class), isNull()))
            .thenReturn(PageIndexResult.ofEmpty());

        DumpPageProcessorResult result = dumpPageProcessor.process(dumpPage);

        assertEquals(DumpPageProcessorResult.PAGE_NOT_PROCESSED, result);

        verify(pageRepository).findByPageId(dumpPageId);
        verify(pageValidator).validateProcessable(any(WikipediaPage.class));
        verify(replacementFinderService).find(any(FinderPage.class));
        verify(pageIndexHelper).indexPageReplacements(any(IndexablePage.class), isNull());
        verify(pageIndexResultSaver, never()).saveBatch(any(PageIndexResult.class));
    }

    @Test
    void testProcessNewPageWithReplacements() throws ReplacerException {
        when(pageRepository.findByPageId(dumpPageId)).thenReturn(Optional.empty());

        Replacement replacement = Replacement
            .builder()
            .start(0)
            .text("")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("")
            .build();
        when(replacementFinderService.find(any(FinderPage.class))).thenReturn(List.of(replacement));

        // No need in this test to build the index result as it would be in the reality with the replacements
        IndexablePage page = IndexablePage.builder().id(dumpPageId).replacements(Collections.emptyList()).build();
        PageIndexResult pageIndexResult = PageIndexResult.builder().createPages(Set.of(page)).build();
        when(pageIndexHelper.indexPageReplacements(any(IndexablePage.class), isNull())).thenReturn(pageIndexResult);

        DumpPageProcessorResult result = dumpPageProcessor.process(dumpPage);

        assertEquals(DumpPageProcessorResult.PAGE_PROCESSED, result);

        verify(pageRepository).findByPageId(dumpPageId);
        verify(pageValidator).validateProcessable(any(WikipediaPage.class));
        verify(replacementFinderService).find(any(FinderPage.class));
        verify(pageIndexHelper).indexPageReplacements(any(IndexablePage.class), isNull());
        verify(pageIndexResultSaver).saveBatch(pageIndexResult);
    }

    @Test
    void testPageNotProcessable() throws ReplacerException {
        when(pageRepository.findByPageId(dumpPageId)).thenReturn(Optional.empty());

        doThrow(ReplacerException.class).when(pageValidator).validateProcessable(any(WikipediaPage.class));

        DumpPageProcessorResult result = dumpPageProcessor.process(dumpPage);

        assertEquals(DumpPageProcessorResult.PAGE_NOT_PROCESSABLE, result);

        verify(pageRepository).findByPageId(dumpPageId);
        verify(pageValidator).validateProcessable(any(WikipediaPage.class));
        verify(replacementFinderService, never()).find(any(FinderPage.class));
        verify(pageIndexHelper, never()).indexPageReplacements(any(IndexablePage.class), any(IndexablePage.class));
        verify(pageIndexResultSaver, never()).saveBatch(any(PageIndexResult.class));
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
        verify(replacementFinderService, never()).find(any(FinderPage.class));
        verify(pageIndexHelper, never()).indexPageReplacements(any(IndexablePage.class), any(IndexablePage.class));
        verify(pageIndexResultSaver, never()).saveBatch(any(PageIndexResult.class));
    }

    @Test
    void testFinish() {
        dumpPageProcessor.finish(WikipediaLanguage.getDefault());

        verify(pageRepository).resetCache();
        verify(pageIndexResultSaver).forceSave();
    }
}
