package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleService;
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
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(yesterday)
                .build();

        Replacement replacement = new Replacement(1, "", "", 1);

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle, Collections.singleton(replacement)));
    }

    @Test
    public void testProcessLastUpdateAfterTimestampForced() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(yesterday)
                .build();

        Replacement replacement = new Replacement(1, "", "", 1);

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, Collections.singleton(replacement), true));
    }

    @Test
    public void testProcessLastUpdateWhenTimestamp() {
        LocalDateTime today = LocalDateTime.now();
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(today)
                .build();

        Replacement replacement = new Replacement(1, "", "", 1);

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle, Collections.singleton(replacement)));
    }

    @Test
    public void testProcessLastUpdateWhenTimestampForced() {
        LocalDateTime today = LocalDateTime.now();
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(today)
                .build();

        Replacement replacement = new Replacement(1, "", "", 1);

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, Collections.singleton(replacement), true));
    }

    @Test
    public void testProcessLastUpdateBeforeTimestamp() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(today)
                .build();

        Replacement replacement = new Replacement(1, "", "", 1).withLastUpdate(yesterday.toLocalDate());

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, Collections.singleton(replacement)));
    }

    @Test
    public void testProcessNewArticle() {
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .build();

        List<ArticleReplacement> articleReplacements = Collections.singletonList(Mockito.mock(ArticleReplacement.class));
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString())).thenReturn(articleReplacements);

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, Collections.emptySet()));

        Mockito.verify(articleService).indexReplacements(Mockito.anyCollection(), Mockito.eq(Collections.emptySet()), Mockito.eq(true));
    }

    @Test
    public void testProcessWithNoReplacementsFound() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(today)
                .build();

        Replacement replacement = new Replacement(1, "", "", 1).withLastUpdate(yesterday.toLocalDate());
        Collection<Replacement> dbReplacements = Collections.singleton(replacement);

        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString())).thenReturn(Collections.emptyList());

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, dbReplacements));

        Mockito.verify(articleService).indexReplacements(Mockito.eq(Collections.emptyList()), Mockito.eq(dbReplacements), Mockito.eq(true));
    }

}
