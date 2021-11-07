package es.bvalero.replacer.dump;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.domain.ReplacerException;
import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import es.bvalero.replacer.page.index.IndexablePage;
import es.bvalero.replacer.page.index.IndexablePageValidator;
import es.bvalero.replacer.page.index.PageIndexHelper;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DumpPageProcessorTest {

    private final String title = "T";
    private final DumpPage dumpPage = DumpPage
        .builder()
        .lang(WikipediaLanguage.SPANISH)
        .id(1)
        .namespace(WikipediaNamespace.ARTICLE)
        .title(title)
        .content("C")
        .lastUpdate(LocalDate.now())
        .build();

    @Mock
    private IndexablePageValidator indexablePageValidator;

    @Mock
    private PageReplacementService pageReplacementService;

    @Mock
    private DumpWriter dumpWriter;

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
        dumpPageProcessor.initializeToWrite();
    }

    @Test
    void testProcessNoReplacements() throws ReplacerException {
        List<ReplacementEntity> toUpdate = Collections.emptyList();
        when(pageIndexHelper.findIndexPageReplacements(any(IndexablePage.class), anyList(), anyList()))
            .thenReturn(toUpdate);

        DumpPageProcessorResult result = dumpPageProcessor.process(dumpPage);

        assertEquals(DumpPageProcessorResult.PAGE_NOT_PROCESSED, result);

        verify(indexablePageValidator).validateProcessable(any(IndexablePage.class));
        verify(pageReplacementService).findByPageId(dumpPage.getId(), dumpPage.getLang());
        verify(replacementFinderService).find(any(FinderPage.class));
        verify(pageIndexHelper).findIndexPageReplacements(any(IndexablePage.class), anyList(), anyList());
    }

    @Test
    void testProcessWithReplacements() throws ReplacerException {
        Replacement pageReplacement = Replacement
            .builder()
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("S")
            .start(0)
            .text("C")
            .build();
        List<Replacement> pageReplacements = List.of(pageReplacement);
        when(replacementFinderService.find(any(FinderPage.class))).thenReturn(pageReplacements);

        ReplacementEntity dbReplacement = ReplacementEntity.builder().build();
        List<ReplacementEntity> toUpdate = List.of(dbReplacement);
        when(pageIndexHelper.findIndexPageReplacements(any(IndexablePage.class), anyList(), anyList()))
            .thenReturn(toUpdate);

        DumpPageProcessorResult result = dumpPageProcessor.process(dumpPage);

        assertEquals(DumpPageProcessorResult.PAGE_PROCESSED, result);

        verify(indexablePageValidator).validateProcessable(any(IndexablePage.class));
        verify(pageReplacementService).findByPageId(dumpPage.getId(), dumpPage.getLang());
        verify(replacementFinderService).find(any(FinderPage.class));
        verify(pageIndexHelper).findIndexPageReplacements(any(IndexablePage.class), anyList(), anyList());
    }

    @Test
    void testPageNoProcessable() throws ReplacerException {
        doThrow(ReplacerException.class).when(indexablePageValidator).validateProcessable(any(IndexablePage.class));

        DumpPageProcessorResult result = dumpPageProcessor.process(dumpPage);

        assertEquals(DumpPageProcessorResult.PAGE_NOT_PROCESSABLE, result);

        verify(indexablePageValidator).validateProcessable(any(IndexablePage.class));
        verify(pageReplacementService).findByPageId(anyInt(), any(WikipediaLanguage.class));
        verify(replacementFinderService, never()).find(any(FinderPage.class));
        verify(pageIndexHelper, never()).findIndexPageReplacements(any(IndexablePage.class), anyList(), anyList());
    }

    @Test
    void testPageNotProcessed() throws ReplacerException {
        LocalDate updated = dumpPage.getLastUpdate().plusDays(1);
        ReplacementEntity dbReplacement = ReplacementEntity.builder().title(title).lastUpdate(updated).build();
        List<ReplacementEntity> dbReplacements = List.of(dbReplacement);
        when(pageReplacementService.findByPageId(dumpPage.getId(), dumpPage.getLang())).thenReturn(dbReplacements);

        DumpPageProcessorResult result = dumpPageProcessor.process(dumpPage);

        assertEquals(DumpPageProcessorResult.PAGE_NOT_PROCESSED, result);

        verify(indexablePageValidator).validateProcessable(any(IndexablePage.class));
        verify(pageReplacementService).findByPageId(dumpPage.getId(), dumpPage.getLang());
        verify(replacementFinderService, never()).find(any(FinderPage.class));
        verify(pageIndexHelper, never()).findIndexPageReplacements(any(IndexablePage.class), anyList(), anyList());
    }
}
