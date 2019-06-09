package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.article.ArticleTimestamp;
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

import java.time.LocalDate;
import java.util.*;

public class DumpArticleProcessorTest {

    @Mock
    private ArticleService articleService;

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
        WikipediaPage dumpArticle = WikipediaPage.builder().setNamespace(WikipediaNamespace.ARTICLE).setContent("").build();
        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testCheckNamespaces() {
        WikipediaPage dumpArticle = WikipediaPage.builder().setNamespace(WikipediaNamespace.ARTICLE).setContent("").build();
        WikipediaPage dumpAnnex = WikipediaPage.builder().setNamespace(WikipediaNamespace.ANNEX).setContent("").build();
        WikipediaPage dumpCategory = WikipediaPage.builder().setNamespace(WikipediaNamespace.CATEGORY).build();

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpAnnex));
        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpCategory));
    }

    @Test
    public void testProcessRedirection() {
        WikipediaPage dumpArticle = WikipediaPage.builder().setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("#Redirect").build();
        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessLastUpdateAfterTimestamp() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(yesterday)
                .build();

        ArticleTimestamp timestamp = new ArticleTimestamp(1, today);
        Mockito.when(articleService.findMaxLastUpdateByArticleIdIn(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Collections.singletonList(timestamp));

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessLastUpdateAfterTimestampForced() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(yesterday)
                .build();

        ArticleTimestamp timestamp = new ArticleTimestamp(1, today);
        Mockito.when(articleService.findMaxLastUpdateByArticleIdIn(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Collections.singletonList(timestamp));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, true));
    }

    @Test
    public void testProcessLastUpdateWhenTimestamp() {
        LocalDate today = LocalDate.now();
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(today)
                .build();

        ArticleTimestamp timestamp = new ArticleTimestamp(1, today);
        Mockito.when(articleService.findMaxLastUpdateByArticleIdIn(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Collections.singletonList(timestamp));

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessLastUpdateWhenTimestampForced() {
        LocalDate today = LocalDate.now();
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(today)
                .build();

        ArticleTimestamp timestamp = new ArticleTimestamp(1, today);
        Mockito.when(articleService.findMaxLastUpdateByArticleIdIn(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Collections.singletonList(timestamp));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, true));
    }

    @Test
    public void testProcessLastUpdateBeforeTimestamp() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = LocalDate.now().minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(today)
                .build();

        ArticleTimestamp timestamp = new ArticleTimestamp(1, yesterday);
        Mockito.when(articleService.findMaxLastUpdateByArticleIdIn(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Collections.singletonList(timestamp));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessNewArticle() {
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .build();

        Mockito.when(articleService.findMaxLastUpdateByArticleIdIn(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Collections.emptyList());
        List<ArticleReplacement> articleReplacements = Collections.singletonList(Mockito.mock(ArticleReplacement.class));
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString()))
                .thenReturn(articleReplacements);

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        dumpArticleProcessor.finish();

        Map<WikipediaPage, Collection<ArticleReplacement>> mapToIndex = new HashMap<>();
        mapToIndex.put(dumpArticle, articleReplacements);
        Mockito.verify(articleService).indexArticleReplacementsInBatch(mapToIndex);
    }

    @Test
    public void testProcessWithNoReplacementsFound() {
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .build();

        ArticleTimestamp timestamp = new ArticleTimestamp(1, LocalDate.now());
        Mockito.when(articleService.findMaxLastUpdateByArticleIdIn(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Collections.singletonList(timestamp));
        List<ArticleReplacement> articleReplacements = Collections.emptyList();
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString()))
                .thenReturn(articleReplacements);

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        dumpArticleProcessor.finish();

        Map<WikipediaPage, Collection<ArticleReplacement>> mapToIndex = new HashMap<>();
        mapToIndex.put(dumpArticle, articleReplacements);
        Mockito.verify(articleService).indexArticleReplacementsInBatch(mapToIndex);
    }

}
