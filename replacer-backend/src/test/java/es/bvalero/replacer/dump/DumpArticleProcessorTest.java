package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleIndexService;
import es.bvalero.replacer.article.Replacement;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DumpArticleProcessorTest {

    @Mock
    private DumpArticleCache dumpArticleCache;

    @Mock
    private ArticleIndexService articleIndexService;

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
        WikipediaPage dumpArticle = WikipediaPage.builder().namespace(WikipediaNamespace.ARTICLE).content("").build();
        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));

        Mockito.verify(dumpArticleCache).findDatabaseReplacements(Mockito.anyInt());
        Mockito.verify(replacementFinderService).findReplacements(Mockito.anyString());
        Mockito.verify(articleIndexService)
                .indexArticleReplacements(Mockito.eq(dumpArticle), Mockito.anyList(), Mockito.anyList());
    }

    @Test
    public void testCheckNamespaces() {
        WikipediaPage dumpArticle = WikipediaPage.builder().namespace(WikipediaNamespace.ARTICLE).content("").build();
        WikipediaPage dumpAnnex = WikipediaPage.builder().namespace(WikipediaNamespace.ANNEX).content("").build();
        WikipediaPage dumpCategory = WikipediaPage.builder().namespace(WikipediaNamespace.CATEGORY).build();

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpAnnex));
        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpCategory));
    }

    @Test
    public void testProcessRedirection() {
        WikipediaPage dumpArticle = WikipediaPage.builder().namespace(WikipediaNamespace.ARTICLE)
                .content("#Redirect").build();
        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessLastUpdateAfterTimestamp() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .lastUpdate(yesterday)
                .build();

        Replacement replacement = new Replacement(1, "", "", 1)
                .withLastUpdate(today.toLocalDate());
        Mockito.when(dumpArticleCache.findDatabaseReplacements(Mockito.anyInt()))
                .thenReturn(Collections.singleton(replacement));

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessLastUpdateAfterTimestampForced() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .lastUpdate(yesterday)
                .build();

        Replacement replacement = new Replacement(1, "", "", 1)
                .withLastUpdate(today.toLocalDate());
        Mockito.when(dumpArticleCache.findDatabaseReplacements(Mockito.anyInt()))
                .thenReturn(Collections.singleton(replacement));

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessLastUpdateWhenTimestamp() {
        LocalDateTime today = LocalDateTime.now();

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .lastUpdate(today)
                .build();

        Replacement replacement = new Replacement(1, "", "", 1)
                .withLastUpdate(today.toLocalDate());
        Mockito.when(dumpArticleCache.findDatabaseReplacements(Mockito.anyInt()))
                .thenReturn(Collections.singleton(replacement));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessLastUpdateBeforeTimestamp() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .lastUpdate(today)
                .build();

        Replacement replacement = new Replacement(1, "", "", 1)
                .withLastUpdate(yesterday.toLocalDate());
        Mockito.when(dumpArticleCache.findDatabaseReplacements(Mockito.anyInt()))
                .thenReturn(Collections.singleton(replacement));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessNewArticle() {
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .build();

        Collection<Replacement> dbReplacements = Collections.emptySet();
        Mockito.when(dumpArticleCache.findDatabaseReplacements(Mockito.anyInt())).thenReturn(dbReplacements);

        ArticleReplacement articleReplacement = ArticleReplacement.builder().build();
        List<ArticleReplacement> articleReplacements = Collections.singletonList(articleReplacement);
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString())).thenReturn(articleReplacements);

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));

        Mockito.verify(articleIndexService).indexArticleReplacements(
                Mockito.eq(dumpArticle), Mockito.eq(articleReplacements), Mockito.eq(dbReplacements));
    }

    @Test
    public void testProcessWithNoReplacementsFound() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .lastUpdate(today)
                .build();

        Replacement replacement = new Replacement(1, "", "", 1)
                .withLastUpdate(yesterday.toLocalDate());
        Collection<Replacement> dbReplacements = Collections.singleton(replacement);
        Mockito.when(dumpArticleCache.findDatabaseReplacements(Mockito.anyInt())).thenReturn(dbReplacements);

        List<ArticleReplacement> articleReplacements = Collections.emptyList();
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString())).thenReturn(articleReplacements);

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));

        Mockito.verify(articleIndexService).indexArticleReplacements(
                Mockito.eq(dumpArticle), Mockito.eq(articleReplacements), Mockito.eq(dbReplacements));
    }

}
