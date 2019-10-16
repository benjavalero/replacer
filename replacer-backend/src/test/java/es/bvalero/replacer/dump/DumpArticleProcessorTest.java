package es.bvalero.replacer.dump;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
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
    private ReplacementFinderService replacementFinderService;

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
        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));

        Mockito.verify(replacementCache).findByArticleId(Mockito.anyInt());
        Mockito.verify(replacementFinderService).findReplacements(Mockito.anyString());
        Mockito.verify(replacementIndexService).indexArticleReplacements(Mockito.anyInt(), Mockito.anyList(), Mockito.anyList());
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

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessLastUpdateAfterTimestampForced() {
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

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
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

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
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

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
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
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString())).thenReturn(replacements);

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));

        Mockito.verify(replacementIndexService).indexArticleReplacements(Mockito.anyInt(), Mockito.anyList(), Mockito.eq(dbReplacements));
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
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString())).thenReturn(replacements);

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));

        Mockito.verify(replacementIndexService).indexArticleReplacements(Mockito.anyInt(), Mockito.anyList(), Mockito.eq(dbReplacements));
    }

}
