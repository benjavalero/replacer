package es.bvalero.replacer.dump;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
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

public class DumpArticleProcessorTest {
    @Mock
    private ReplacementCache replacementCache;

    @Mock
    private ReplacementIndexService replacementIndexService;

    @Mock
    private ReplacementFindService replacementFindService;

    @InjectMocks
    private DumpArticleProcessor dumpArticleProcessor;

    @BeforeEach
    public void setUp() {
        dumpArticleProcessor = new DumpArticleProcessor();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessSimple() {
        DumpArticle dumpArticle = DumpArticle
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("")
            .build();
        dumpArticleProcessor.processArticle(dumpArticle);

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
    public void testCheckNamespaces() {
        DumpArticle dumpArticle = DumpArticle
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("")
            .build();
        DumpArticle dumpAnnex = DumpArticle.builder().namespace(WikipediaNamespace.ANNEX).content("").build();
        DumpArticle dumpCategory = DumpArticle.builder().namespace(WikipediaNamespace.CATEGORY).build();

        Assertions.assertTrue(dumpArticle.isProcessable());
        Assertions.assertTrue(dumpAnnex.isProcessable());
        Assertions.assertFalse(dumpCategory.isProcessable());
    }

    @Test
    public void testProcessRedirection() {
        DumpArticle dumpArticle = DumpArticle
            .builder()
            .lang(WikipediaLanguage.SPANISH)
            .namespace(WikipediaNamespace.ARTICLE)
            .content("#Redirect")
            .build();
        Assertions.assertFalse(dumpArticle.isProcessable());
    }

    @Test
    public void testProcessLastUpdateAfterTimestamp() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        DumpArticle dumpArticle = DumpArticle
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

        Assertions.assertTrue(dumpArticleProcessor.processArticle(dumpArticle).isEmpty());
        Mockito
            .verify(replacementFindService, Mockito.times(0))
            .findReplacements(Mockito.anyString(), Mockito.any(WikipediaLanguage.class));
    }

    @Test
    public void testProcessLastUpdateWhenTimestamp() {
        LocalDate today = LocalDate.now();

        DumpArticle dumpArticle = DumpArticle
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

        dumpArticleProcessor.processArticle(dumpArticle);
        Mockito
            .verify(replacementFindService)
            .findReplacements(Mockito.anyString(), Mockito.any(WikipediaLanguage.class));
    }

    @Test
    public void testProcessLastUpdateBeforeTimestamp() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        DumpArticle dumpArticle = DumpArticle
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

        dumpArticleProcessor.processArticle(dumpArticle);
        Mockito
            .verify(replacementFindService)
            .findReplacements(Mockito.anyString(), Mockito.any(WikipediaLanguage.class));
    }

    @Test
    public void testProcessNewArticle() {
        DumpArticle dumpArticle = DumpArticle
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

        dumpArticleProcessor.processArticle(dumpArticle);

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
    public void testProcessWithNoReplacementsFound() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        DumpArticle dumpArticle = DumpArticle
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

        dumpArticleProcessor.processArticle(dumpArticle);

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
