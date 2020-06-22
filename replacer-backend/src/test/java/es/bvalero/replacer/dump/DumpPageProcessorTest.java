package es.bvalero.replacer.dump;

import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = XmlConfiguration.class)
class DumpPageProcessorTest {
    @Resource
    private List<String> ignorableTemplates;

    @Mock
    private ReplacementCache replacementCache;

    @Mock
    private ReplacementIndexService replacementIndexService;

    @Mock
    private ReplacementFindService replacementFindService;

    @InjectMocks
    private DumpPageProcessor dumpPageProcessor;

    @BeforeEach
    public void setUp() {
        dumpPageProcessor = new DumpPageProcessor();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testProcessSimple() {
        DumpPage dumpArticle = DumpPage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("")
            .build();
        dumpPageProcessor.processArticle(dumpArticle);

        Mockito.verify(replacementCache).findByArticleId(Mockito.anyInt(), Mockito.any(WikipediaLanguage.class));
        Mockito
            .verify(replacementFindService)
            .findReplacements(Mockito.anyString(), Mockito.any(WikipediaLanguage.class));
        Mockito
            .verify(replacementIndexService)
            .findIndexArticleReplacements(
                Mockito.anyInt(),
                Mockito.any(WikipediaLanguage.class),
                Mockito.anyList(),
                Mockito.anyList()
            );
    }

    @Test
    void testCheckNamespaces() {
        DumpPage dumpArticle = DumpPage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("")
            .build();
        DumpPage dumpAnnex = DumpPage.builder().namespace(WikipediaNamespace.ANNEX).content("").build();
        DumpPage dumpCategory = DumpPage.builder().namespace(WikipediaNamespace.CATEGORY).build();

        Assertions.assertTrue(dumpArticle.isProcessable(ignorableTemplates));
        Assertions.assertTrue(dumpAnnex.isProcessable(ignorableTemplates));
        Assertions.assertFalse(dumpCategory.isProcessable(ignorableTemplates));
    }

    @Test
    void testProcessRedirection() {
        DumpPage dumpArticle = DumpPage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("#Redirect")
            .build();
        Assertions.assertFalse(dumpArticle.isProcessable(ignorableTemplates));
    }

    @Test
    void testProcessLastUpdateAfterTimestamp() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        DumpPage dumpArticle = DumpPage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("")
            .lastUpdate(yesterday)
            .build();

        ReplacementEntity replacement = new ReplacementEntity(1, "", "", 1);
        replacement.setLastUpdate(today);
        Mockito
            .when(replacementCache.findByArticleId(Mockito.anyInt(), Mockito.any(WikipediaLanguage.class)))
            .thenReturn(Collections.singletonList(replacement));

        Assertions.assertTrue(dumpPageProcessor.processArticle(dumpArticle).isEmpty());
        Mockito
            .verify(replacementFindService, Mockito.times(0))
            .findReplacements(Mockito.anyString(), Mockito.any(WikipediaLanguage.class));
    }

    @Test
    void testProcessLastUpdateWhenTimestamp() {
        LocalDate today = LocalDate.now();

        DumpPage dumpArticle = DumpPage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("")
            .lastUpdate(today)
            .build();

        ReplacementEntity replacement = new ReplacementEntity(1, "", "", 1);
        replacement.setLastUpdate(today);
        Mockito
            .when(replacementCache.findByArticleId(Mockito.anyInt(), Mockito.any(WikipediaLanguage.class)))
            .thenReturn(Collections.singletonList(replacement));

        dumpPageProcessor.processArticle(dumpArticle);
        Mockito
            .verify(replacementFindService)
            .findReplacements(Mockito.anyString(), Mockito.any(WikipediaLanguage.class));
    }

    @Test
    void testProcessLastUpdateBeforeTimestamp() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        DumpPage dumpArticle = DumpPage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("")
            .lastUpdate(today)
            .build();

        ReplacementEntity replacement = new ReplacementEntity(1, "", "", 1);
        replacement.setLastUpdate(yesterday);
        Mockito
            .when(replacementCache.findByArticleId(Mockito.anyInt(), Mockito.any(WikipediaLanguage.class)))
            .thenReturn(Collections.singletonList(replacement));

        dumpPageProcessor.processArticle(dumpArticle);
        Mockito
            .verify(replacementFindService)
            .findReplacements(Mockito.anyString(), Mockito.any(WikipediaLanguage.class));
    }

    @Test
    void testProcessNewArticle() {
        DumpPage dumpArticle = DumpPage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("")
            .lastUpdate(LocalDate.now())
            .build();

        List<ReplacementEntity> dbReplacements = Collections.emptyList();
        Mockito
            .when(replacementCache.findByArticleId(Mockito.anyInt(), Mockito.any(WikipediaLanguage.class)))
            .thenReturn(dbReplacements);

        Replacement replacement = Replacement.builder().build();
        List<Replacement> replacements = Collections.singletonList(replacement);
        Mockito
            .when(replacementFindService.findReplacements(Mockito.anyString(), Mockito.any(WikipediaLanguage.class)))
            .thenReturn(replacements);

        dumpPageProcessor.processArticle(dumpArticle);

        Mockito
            .verify(replacementIndexService)
            .findIndexArticleReplacements(
                Mockito.anyInt(),
                Mockito.any(WikipediaLanguage.class),
                Mockito.anyList(),
                Mockito.eq(dbReplacements)
            );
    }

    @Test
    void testProcessWithNoReplacementsFound() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        DumpPage dumpArticle = DumpPage
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("")
            .lastUpdate(today)
            .build();

        ReplacementEntity replacement = new ReplacementEntity(1, "", "", 1);
        replacement.setLastUpdate(yesterday);
        List<ReplacementEntity> dbReplacements = Collections.singletonList(replacement);
        Mockito
            .when(replacementCache.findByArticleId(Mockito.anyInt(), Mockito.any(WikipediaLanguage.class)))
            .thenReturn(dbReplacements);

        List<Replacement> replacements = Collections.emptyList();
        Mockito
            .when(replacementFindService.findReplacements(Mockito.anyString(), Mockito.any(WikipediaLanguage.class)))
            .thenReturn(replacements);

        dumpPageProcessor.processArticle(dumpArticle);

        Mockito
            .verify(replacementIndexService)
            .findIndexArticleReplacements(
                Mockito.anyInt(),
                Mockito.any(WikipediaLanguage.class),
                Mockito.anyList(),
                Mockito.eq(dbReplacements)
            );
    }
}
