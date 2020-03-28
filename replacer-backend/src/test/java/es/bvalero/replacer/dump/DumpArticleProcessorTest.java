package es.bvalero.replacer.dump;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class DumpArticleProcessorTest {

    @Mock
    private ReplacementCache replacementCache;

    @Mock
    private ReplacementIndexService replacementIndexService;

    @Mock
    private ReplacementFindService replacementFindService;

    @InjectMocks
    private DumpArticleProcessor dumpArticleProcessor;

    @Before
    public void setUp() {
        dumpArticleProcessor = new DumpArticleProcessor();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessSimple() {
        DumpArticle dumpArticle = DumpArticle.builder().namespace(WikipediaNamespace.ARTICLE).content("").build();
        dumpArticleProcessor.processArticle(dumpArticle);

        Mockito.verify(replacementCache).findByArticleId(Mockito.anyInt());
        Mockito.verify(replacementFindService).findReplacements(Mockito.anyString());
        Mockito.verify(replacementIndexService).findIndexArticleReplacements(Mockito.anyInt(), Mockito.anyList(), Mockito.anyList());
    }

    @Test
    public void testCheckNamespaces() {
        DumpArticle dumpArticle = DumpArticle.builder().namespace(WikipediaNamespace.ARTICLE).content("").build();
        DumpArticle dumpAnnex = DumpArticle.builder().namespace(WikipediaNamespace.ANNEX).content("").build();
        DumpArticle dumpCategory = DumpArticle.builder().namespace(WikipediaNamespace.CATEGORY).build();

        Assert.assertTrue(dumpArticle.isProcessable());
        Assert.assertTrue(dumpAnnex.isProcessable());
        Assert.assertFalse(dumpCategory.isProcessable());
    }

    @Test
    public void testProcessRedirection() {
        DumpArticle dumpArticle = DumpArticle.builder().namespace(WikipediaNamespace.ARTICLE)
                .content("#Redirect").build();
        Assert.assertFalse(dumpArticle.isProcessable());
    }

    @Test
    public void testProcessLastUpdateAfterTimestamp() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        DumpArticle dumpArticle = DumpArticle.builder()
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .lastUpdate(yesterday)
                .build();

        ReplacementEntity replacement = new ReplacementEntity(1, "", "", 1);
        replacement.setLastUpdate(today);
        Mockito.when(replacementCache.findByArticleId(Mockito.anyInt()))
                .thenReturn(Collections.singletonList(replacement));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle).isEmpty());
        Mockito.verify(replacementFindService, Mockito.times(0)).findReplacements(Mockito.anyString());
    }

    @Test
    public void testProcessLastUpdateWhenTimestamp() {
        LocalDate today = LocalDate.now();

        DumpArticle dumpArticle = DumpArticle.builder()
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .lastUpdate(today)
                .build();

        ReplacementEntity replacement = new ReplacementEntity(1, "", "", 1);
        replacement.setLastUpdate(today);
        Mockito.when(replacementCache.findByArticleId(Mockito.anyInt()))
                .thenReturn(Collections.singletonList(replacement));

        dumpArticleProcessor.processArticle(dumpArticle);
        Mockito.verify(replacementFindService).findReplacements(Mockito.anyString());
    }

    @Test
    public void testProcessLastUpdateBeforeTimestamp() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        DumpArticle dumpArticle = DumpArticle.builder()
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .lastUpdate(today)
                .build();

        ReplacementEntity replacement = new ReplacementEntity(1, "", "", 1);
        replacement.setLastUpdate(yesterday);
        Mockito.when(replacementCache.findByArticleId(Mockito.anyInt()))
                .thenReturn(Collections.singletonList(replacement));

        dumpArticleProcessor.processArticle(dumpArticle);
        Mockito.verify(replacementFindService).findReplacements(Mockito.anyString());
    }

    @Test
    public void testProcessNewArticle() {
        DumpArticle dumpArticle = DumpArticle.builder()
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .lastUpdate(LocalDate.now())
                .build();

        List<ReplacementEntity> dbReplacements = Collections.emptyList();
        Mockito.when(replacementCache.findByArticleId(Mockito.anyInt())).thenReturn(dbReplacements);

        Replacement replacement = Replacement.builder().build();
        List<Replacement> replacements = Collections.singletonList(replacement);
        Mockito.when(replacementFindService.findReplacements(Mockito.anyString())).thenReturn(replacements);

        dumpArticleProcessor.processArticle(dumpArticle);

        Mockito.verify(replacementIndexService).findIndexArticleReplacements(Mockito.anyInt(), Mockito.anyList(), Mockito.eq(dbReplacements));
    }

    @Test
    public void testProcessWithNoReplacementsFound() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        DumpArticle dumpArticle = DumpArticle.builder()
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .lastUpdate(today)
                .build();

        ReplacementEntity replacement = new ReplacementEntity(1, "", "", 1);
        replacement.setLastUpdate(yesterday);
        List<ReplacementEntity> dbReplacements = Collections.singletonList(replacement);
        Mockito.when(replacementCache.findByArticleId(Mockito.anyInt())).thenReturn(dbReplacements);

        List<Replacement> replacements = Collections.emptyList();
        Mockito.when(replacementFindService.findReplacements(Mockito.anyString())).thenReturn(replacements);

        dumpArticleProcessor.processArticle(dumpArticle);

        Mockito.verify(replacementIndexService).findIndexArticleReplacements(Mockito.anyInt(), Mockito.anyList(), Mockito.eq(dbReplacements));
    }

}
