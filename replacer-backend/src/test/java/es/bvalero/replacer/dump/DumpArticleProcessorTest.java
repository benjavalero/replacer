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
                .id(1)
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .lastUpdate(yesterday)
                .build();

        Replacement replacement = new Replacement(1, "", "", 1);

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle, Collections.singleton(replacement)));
    }

    @Test
    public void testProcessLastUpdateAfterTimestampForced() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .id(1)
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .lastUpdate(yesterday)
                .build();

        Replacement replacement = new Replacement(1, "", "", 1);

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle, Collections.singleton(replacement), true));
    }

    @Test
    public void testProcessLastUpdateWhenTimestamp() {
        LocalDateTime today = LocalDateTime.now();
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .id(1)
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .lastUpdate(today)
                .build();

        Replacement replacement = new Replacement(1, "", "", 1);

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle, Collections.singleton(replacement)));
    }

    @Test
    public void testProcessLastUpdateWhenTimestampForced() {
        LocalDateTime today = LocalDateTime.now();
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .id(1)
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .lastUpdate(today)
                .build();

        Replacement replacement = new Replacement(1, "", "", 1);

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, Collections.singleton(replacement), true));
    }

    @Test
    public void testProcessLastUpdateBeforeTimestamp() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .id(1)
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .lastUpdate(today)
                .build();

        Replacement replacement = new Replacement(1, "", "", 1).withLastUpdate(yesterday.toLocalDate());

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, Collections.singleton(replacement)));
    }

    @Test
    public void testProcessNewArticle() {
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .build();

        List<ArticleReplacement> articleReplacements = Collections.singletonList(Mockito.mock(ArticleReplacement.class));
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString())).thenReturn(articleReplacements);

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, Collections.emptySet()));

        Mockito.verify(articleIndexService).indexReplacements(
                Mockito.eq(dumpArticle), Mockito.anyCollection(), Mockito.eq(Collections.emptySet()), Mockito.eq(true));
    }

    @Test
    public void testProcessWithNoReplacementsFound() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .id(1)
                .namespace(WikipediaNamespace.ARTICLE)
                .content("")
                .lastUpdate(today)
                .build();

        Replacement replacement = new Replacement(1, "", "", 1).withLastUpdate(yesterday.toLocalDate());
        Collection<Replacement> dbReplacements = Collections.singleton(replacement);

        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString())).thenReturn(Collections.emptyList());

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, dbReplacements));

        Mockito.verify(articleIndexService).indexReplacements(
                Mockito.eq(dumpArticle), Mockito.eq(Collections.emptyList()), Mockito.eq(dbReplacements), Mockito.eq(true));
    }

}
