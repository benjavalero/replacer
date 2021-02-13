package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.common.WikipediaNamespace;
import es.bvalero.replacer.finder.common.FinderPage;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.page.IndexablePage;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class DumpPageProcessorTest {

    @Mock
    private PageReplacementService pageReplacementService;

    @Mock
    private ReplacementIndexService replacementIndexService;

    @Mock
    private ReplacementFinderService replacementFinderService;

    @InjectMocks
    private DumpPageProcessor dumpPageProcessor;

    @BeforeEach
    public void setUp() {
        dumpPageProcessor = new DumpPageProcessor();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testProcessSimple() {
        DumpPage dumpPage = DumpPage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("")
            .build();
        dumpPageProcessor.processPage(dumpPage);

        Mockito.verify(pageReplacementService).findByPageId(Mockito.anyInt(), Mockito.any(WikipediaLanguage.class));
        Mockito.verify(replacementFinderService).findList(Mockito.any(FinderPage.class));
        Mockito
            .verify(replacementIndexService)
            .findIndexPageReplacements(Mockito.any(IndexablePage.class), Mockito.anyList(), Mockito.anyList());
    }

    @Test
    void testCheckNamespaces() throws ReplacerException {
        DumpPage dumpPage = DumpPage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("")
            .build();
        DumpPage dumpAnnex = DumpPage.builder().namespace(WikipediaNamespace.ANNEX).content("").build();
        DumpPage dumpCategory = DumpPage.builder().namespace(WikipediaNamespace.CATEGORY).build();

        dumpPage.validateProcessable();
        dumpAnnex.validateProcessable();
        Assertions.assertThrows(ReplacerException.class, dumpCategory::validateProcessable);
    }

    @Test
    void testProcessLastUpdateAfterTimestamp() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        DumpPage dumpPage = DumpPage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("")
            .lastUpdate(yesterday)
            .build();

        ReplacementEntity replacement = ReplacementEntity.of(1, "", "", 1).withLastUpdate(today);
        Mockito
            .when(pageReplacementService.findByPageId(Mockito.anyInt(), Mockito.any(WikipediaLanguage.class)))
            .thenReturn(Collections.singletonList(replacement));

        Assertions.assertTrue(dumpPageProcessor.processPage(dumpPage).isEmpty());
        Mockito.verify(replacementFinderService, Mockito.times(0)).findList(Mockito.any(FinderPage.class));
    }

    @Test
    void testProcessLastUpdateWhenTimestamp() {
        LocalDate today = LocalDate.now();

        DumpPage dumpPage = DumpPage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("")
            .lastUpdate(today)
            .build();

        ReplacementEntity replacement = ReplacementEntity.of(1, "", "", 1).withLastUpdate(today);
        Mockito
            .when(pageReplacementService.findByPageId(Mockito.anyInt(), Mockito.any(WikipediaLanguage.class)))
            .thenReturn(Collections.singletonList(replacement));

        dumpPageProcessor.processPage(dumpPage);
        Mockito.verify(replacementFinderService).findList(Mockito.any(FinderPage.class));
    }

    @Test
    void testProcessLastUpdateBeforeTimestamp() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        DumpPage dumpPage = DumpPage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("")
            .lastUpdate(today)
            .build();

        ReplacementEntity replacement = ReplacementEntity.of(1, "", "", 1).withLastUpdate(yesterday);
        Mockito
            .when(pageReplacementService.findByPageId(Mockito.anyInt(), Mockito.any(WikipediaLanguage.class)))
            .thenReturn(Collections.singletonList(replacement));

        dumpPageProcessor.processPage(dumpPage);
        Mockito.verify(replacementFinderService).findList(Mockito.any(FinderPage.class));
    }

    @Test
    void testProcessNewPage() {
        DumpPage dumpPage = DumpPage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("X")
            .lastUpdate(LocalDate.now())
            .build();

        List<ReplacementEntity> dbReplacements = Collections.emptyList();
        Mockito
            .when(pageReplacementService.findByPageId(Mockito.anyInt(), Mockito.any(WikipediaLanguage.class)))
            .thenReturn(dbReplacements);

        Replacement replacement = Replacement.builder().start(0).text("X").build();
        List<Replacement> replacements = Collections.singletonList(replacement);
        Mockito.when(replacementFinderService.findList(Mockito.any(FinderPage.class))).thenReturn(replacements);

        dumpPageProcessor.processPage(dumpPage);

        Mockito
            .verify(replacementIndexService)
            .findIndexPageReplacements(Mockito.any(IndexablePage.class), Mockito.anyList(), Mockito.eq(dbReplacements));
    }

    @Test
    void testProcessWithNoReplacementsFound() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        DumpPage dumpPage = DumpPage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("")
            .lastUpdate(today)
            .build();

        ReplacementEntity replacement = ReplacementEntity.of(1, "", "", 1).withLastUpdate(yesterday);
        List<ReplacementEntity> dbReplacements = Collections.singletonList(replacement);
        Mockito
            .when(pageReplacementService.findByPageId(Mockito.anyInt(), Mockito.any(WikipediaLanguage.class)))
            .thenReturn(dbReplacements);

        List<Replacement> replacements = Collections.emptyList();
        Mockito.when(replacementFinderService.findList(Mockito.any(FinderPage.class))).thenReturn(replacements);

        dumpPageProcessor.processPage(dumpPage);

        Mockito
            .verify(replacementIndexService)
            .findIndexPageReplacements(Mockito.any(IndexablePage.class), Mockito.anyList(), Mockito.eq(dbReplacements));
    }
}
