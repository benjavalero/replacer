package es.bvalero.replacer.dump;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.domain.ReplacerException;
import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import es.bvalero.replacer.page.index.PageIndexHelper;
import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.page.index.PageIndexResultSaver;
import es.bvalero.replacer.page.repository.IndexablePage;
import es.bvalero.replacer.page.repository.IndexablePageId;
import es.bvalero.replacer.page.repository.IndexablePageRepository;
import es.bvalero.replacer.page.repository.IndexableReplacement;
import es.bvalero.replacer.page.validate.PageValidator;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import java.time.LocalDate;
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
        .namespace(WikipediaNamespace.ARTICLE)
        .title("T")
        .content("C")
        .lastUpdate(LocalDate.now())
        .build();
    private final IndexablePageId dumpPageId = IndexablePageId.of(dumpPage.getLang(), dumpPage.getId());

    @Mock
    private PageValidator pageValidator;

    @Mock
    private IndexablePageRepository indexablePageRepository;

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
        when(pageIndexHelper.findIndexPageReplacements(any(IndexablePage.class), isNull()))
            .thenReturn(PageIndexResult.ofEmpty());

        DumpPageProcessorResult result = dumpPageProcessor.process(dumpPage);

        assertEquals(DumpPageProcessorResult.PAGE_NOT_PROCESSED, result);

        verify(indexablePageRepository).findByPageId(dumpPageId);
        verify(pageValidator).validateProcessable(dumpPage);
        verify(replacementFinderService).find(any(FinderPage.class));
        verify(pageIndexHelper).findIndexPageReplacements(any(IndexablePage.class), isNull());
        verify(pageIndexResultSaver, never()).saveBatch(any(PageIndexResult.class));
    }

    @Test
    void testProcessNewPageWithReplacements() throws ReplacerException {
        when(indexablePageRepository.findByPageId(dumpPageId)).thenReturn(Optional.empty());

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
        when(pageIndexHelper.findIndexPageReplacements(any(IndexablePage.class), isNull()))
            .thenReturn(pageIndexResult);

        DumpPageProcessorResult result = dumpPageProcessor.process(dumpPage);

        assertEquals(DumpPageProcessorResult.PAGE_PROCESSED, result);

        verify(indexablePageRepository).findByPageId(dumpPageId);
        verify(pageValidator).validateProcessable(dumpPage);
        verify(replacementFinderService).find(any(FinderPage.class));
        verify(pageIndexHelper).findIndexPageReplacements(any(IndexablePage.class), isNull());
        verify(pageIndexResultSaver).saveBatch(pageIndexResult);
    }

    @Test
    void testPageNotProcessable() throws ReplacerException {
        when(indexablePageRepository.findByPageId(dumpPageId)).thenReturn(Optional.empty());

        doThrow(ReplacerException.class).when(pageValidator).validateProcessable(dumpPage);

        DumpPageProcessorResult result = dumpPageProcessor.process(dumpPage);

        assertEquals(DumpPageProcessorResult.PAGE_NOT_PROCESSABLE, result);

        verify(indexablePageRepository).findByPageId(dumpPageId);
        verify(pageValidator).validateProcessable(dumpPage);
        verify(replacementFinderService, never()).find(any(FinderPage.class));
        verify(pageIndexHelper, never())
            .findIndexPageReplacements(any(IndexablePage.class), any(IndexablePage.class));
        verify(pageIndexResultSaver, never()).saveBatch(any(PageIndexResult.class));
    }

    @Test
    void testPageNotProcessedByTimestamp() throws ReplacerException {
        // The page exists in DB but has been updated after the dump
        LocalDate updated = dumpPage.getLastUpdate().plusDays(1);
        IndexableReplacement dbReplacement = IndexableReplacement
            .builder()
            .indexablePageId(dumpPageId)
            .type("")
            .subtype("")
            .position(0)
            .context("")
            .lastUpdate(updated)
            .build();
        IndexablePage dbPage = IndexablePage
            .builder()
            .id(dumpPageId)
            .title(dumpPage.getTitle()) // The title must match so the page is not processed
            .replacements(List.of(dbReplacement))
            .build();
        when(indexablePageRepository.findByPageId(dumpPageId)).thenReturn(Optional.of(dbPage));

        DumpPageProcessorResult result = dumpPageProcessor.process(dumpPage);

        assertEquals(DumpPageProcessorResult.PAGE_NOT_PROCESSED, result);

        verify(indexablePageRepository).findByPageId(dumpPageId);
        verify(pageValidator).validateProcessable(dumpPage);
        verify(replacementFinderService, never()).find(any(FinderPage.class));
        verify(pageIndexHelper, never())
            .findIndexPageReplacements(any(IndexablePage.class), any(IndexablePage.class));
        verify(pageIndexResultSaver, never()).saveBatch(any(PageIndexResult.class));
    }

    @Test
    void testFinish() {
        dumpPageProcessor.finish(WikipediaLanguage.getDefault());

        verify(indexablePageRepository).resetCache(WikipediaLanguage.getDefault());
        verify(pageIndexResultSaver).forceSave();
    }
}
