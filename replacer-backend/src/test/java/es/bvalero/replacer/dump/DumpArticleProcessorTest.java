package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.persistence.Article;
import es.bvalero.replacer.persistence.ArticleRepository;
import es.bvalero.replacer.persistence.Replacement;
import es.bvalero.replacer.persistence.ReplacementRepository;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class DumpArticleProcessorTest {

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ReplacementRepository replacementRepository;

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
    public void testProcessReviewedAfterTimestamp() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(yesterday)
                .build();

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getLastUpdate()).thenReturn(today);
        Mockito.when(articleRepository.findByIdGreaterThanOrderById(Mockito.anyInt(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(dbArticle));

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessReviewedAfterTimestampForced() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(yesterday)
                .build();

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getLastUpdate()).thenReturn(today);
        Mockito.when(articleRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(dbArticle));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, true));
    }

    @Test
    public void testProcessLastUpdateWhenTimestampAndReviewed() {
        LocalDate today = LocalDate.now();
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(today)
                .build();

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getLastUpdate()).thenReturn(today);
        Mockito.when(articleRepository.findByIdGreaterThanOrderById(Mockito.anyInt(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(dbArticle));
        Mockito.when(replacementRepository.findByArticle(dbArticle)).thenReturn(Collections.emptyList());

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessLastUpdateWhenTimestampAndNotReviewed() {
        LocalDate today = LocalDate.now();
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(today)
                .build();

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getLastUpdate()).thenReturn(today);
        Mockito.when(articleRepository.findByIdGreaterThanOrderById(Mockito.anyInt(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(dbArticle));
        Replacement replacement = Mockito.mock(Replacement.class);
        Mockito.when(replacementRepository.findByArticle(dbArticle)).thenReturn(Collections.singletonList(replacement));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessLastUpdateWhenTimestampAndReviewedAndForced() {
        LocalDate today = LocalDate.now();
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(today)
                .build();

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getLastUpdate()).thenReturn(today);
        Mockito.when(articleRepository.findByIdGreaterThanOrderById(Mockito.anyInt(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(dbArticle));
        Mockito.when(replacementRepository.findByArticle(dbArticle)).thenReturn(Collections.emptyList());

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, true));
    }

    @Test
    public void testProcessLastUpdateBeforeTimestamp() {
        LocalDate yesterday = LocalDate.now().minusDays(1L);

        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .build();

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getLastUpdate()).thenReturn(yesterday);
        Mockito.when(articleRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(dbArticle));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
    }

    /* DATABASE TESTS */

    @Test
    public void testProcessNewArticle() {
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .build();

        ArticleReplacement articleReplacement = Mockito.mock(ArticleReplacement.class);
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString()))
                .thenReturn(Collections.singletonList(articleReplacement));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        dumpArticleProcessor.finish();

        Mockito.verify(articleRepository).saveAll(Mockito.anyList());
        Mockito.verify(replacementRepository).saveAll(Mockito.anySet());
    }

    @Test
    public void testProcessExistingArticleWithReplacementChanges() {
        LocalDate now = LocalDate.now();
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(now)
                .build();

        // Let's suppose the article exists in DB
        Article dbArticle = Article.builder().setTitle("").setLastUpdate(now).build();
        Mockito.when(articleRepository.findByIdGreaterThanOrderById(Mockito.anyInt(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(dbArticle));
        // And it has replacements 1 and 2
        Replacement replacement1 = new Replacement.ReplacementBuilder()
                .setArticle(dbArticle)
                .setText("1")
                .build();
        Replacement replacement2 = new Replacement.ReplacementBuilder()
                .setArticle(dbArticle)
                .setText("2")
                .build();
        Mockito.when(replacementRepository.findByArticle(dbArticle)).thenReturn(Arrays.asList(replacement1, replacement2));
        // And the new ones are 2 and 3
        ArticleReplacement articleReplacement2 = Mockito.mock(ArticleReplacement.class);
        Mockito.when(articleReplacement2.getSubtype()).thenReturn("2");
        ArticleReplacement articleReplacement3 = Mockito.mock(ArticleReplacement.class);
        Mockito.when(articleReplacement3.getSubtype()).thenReturn("3");
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString()))
                .thenReturn(Arrays.asList(articleReplacement2, articleReplacement3));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        dumpArticleProcessor.finish();

        Mockito.verify(replacementRepository).deleteInBatch(Mockito.anyList());
        Mockito.verify(articleRepository).saveAll(Mockito.anyList());
        Mockito.verify(replacementRepository).saveAll(Mockito.anySet());
    }

    @Test
    public void testProcessExistingArticleWithNoReplacementChanges() {
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .build();

        // Let's suppose the article exists in DB
        Article dbArticle = Article.builder().build();
        Mockito.when(articleRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(dbArticle));
        // And it has replacement 1
        Replacement replacement1 = new Replacement.ReplacementBuilder()
                .setArticle(dbArticle)
                .setText("1")
                .build();
        Mockito.when(replacementRepository.findByArticle(dbArticle)).thenReturn(Collections.singletonList(replacement1));
        // And the new one is 1
        ArticleReplacement articleReplacement1 = Mockito.mock(ArticleReplacement.class);
        Mockito.when(articleReplacement1.getSubtype()).thenReturn("1");
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString()))
                .thenReturn(Collections.singletonList(articleReplacement1));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));

        Mockito.verify(articleRepository, Mockito.times(0)).save(Mockito.any(Article.class));
        Mockito.verify(replacementRepository, Mockito.times(0)).delete(Mockito.any(Replacement.class));
        Mockito.verify(replacementRepository, Mockito.times(0)).save(Mockito.any(Replacement.class));
    }

    @Test
    public void testProcessExistingArticleWithNoReplacementsFound() {
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(LocalDate.now())
                .build();

        // Let's suppose the article exists in DB
        Article dbArticle = Article.builder().build();
        Mockito.when(articleRepository.findByIdGreaterThanOrderById(Mockito.anyInt(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(dbArticle));
        // And it has replacements 1 and 2
        Replacement replacement1 = new Replacement.ReplacementBuilder()
                .setArticle(dbArticle)
                .setText("1")
                .build();
        Replacement replacement2 = new Replacement.ReplacementBuilder()
                .setArticle(dbArticle)
                .setText("2")
                .build();
        Mockito.when(replacementRepository.findByArticle(dbArticle)).thenReturn(Arrays.asList(replacement1, replacement2));
        // And there are no replacements found
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString())).thenReturn(Collections.emptyList());

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        dumpArticleProcessor.finish();

        Mockito.verify(articleService).deleteArticle(Mockito.any(Article.class));
    }
/*
    @Test
    public void testProcessNewArticleAndDeleteObsolete() {
        // New article
        WikipediaPage dumpArticle = WikipediaPage.builder()
                .setId(2)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .build();

        // Obsolete article in DB
        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getId()).thenReturn(1);
        Mockito.when(articleRepository.findByIdGreaterThanOrderById(Mockito.anyInt(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(dbArticle));

        ArticleReplacement articleReplacement = ArticleReplacement.builder()
                .setType(ReplacementType.MISSPELLING)
                .setSubtype("")
                .build();
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString()))
                .thenReturn(Collections.singletonList(articleReplacement));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        dumpArticleProcessor.finish();

        Mockito.verify(articleService).deleteArticle(Mockito.any(Article.class));
        Mockito.verify(articleRepository).saveAll(Mockito.anyList());
        Mockito.verify(replacementRepository).saveAll(Mockito.anySet());
    }
*/
}
