package es.bvalero.replacer.dump;

import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.persistence.*;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.threeten.bp.LocalDate;

import java.util.Arrays;
import java.util.Collections;

public class DumpArticleProcessorTest {

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
        DumpArticle dumpArticle = DumpArticle.builder().setNamespace(WikipediaNamespace.ARTICLE).setContent("").build();
        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testCheckNamespaces() {
        DumpArticle dumpArticle = DumpArticle.builder().setNamespace(WikipediaNamespace.ARTICLE).setContent("").build();
        DumpArticle dumpAnnex = DumpArticle.builder().setNamespace(WikipediaNamespace.ANNEX).setContent("").build();
        DumpArticle dumpCategory = DumpArticle.builder().setNamespace(WikipediaNamespace.CATEGORY).build();

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpAnnex));
        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpCategory));
    }

    @Test
    public void testProcessRedirection() {
        DumpArticle dumpArticle = DumpArticle.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("#REDIRECT xxx")
                .build();
        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessReviewedAfterTimestamp() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1L);

        DumpArticle dumpArticle = DumpArticle.builder()
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

        DumpArticle dumpArticle = DumpArticle.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(yesterday)
                .build();

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getLastUpdate()).thenReturn(today);
        Mockito.when(articleRepository.findOne(Mockito.anyInt())).thenReturn(dbArticle);

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, true));
    }

    @Test
    public void testProcessLastUpdateWhenTimestampAndReviewed() {
        LocalDate today = LocalDate.now();
        DumpArticle dumpArticle = DumpArticle.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(today)
                .build();

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getLastUpdate()).thenReturn(today);
        Mockito.when(articleRepository.findByIdGreaterThanOrderById(Mockito.anyInt(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(dbArticle));
        Mockito.when(replacementRepository.findByArticle(dbArticle)).thenReturn(Collections.<Replacement>emptyList());

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessLastUpdateWhenTimestampAndNotReviewed() {
        LocalDate today = LocalDate.now();
        DumpArticle dumpArticle = DumpArticle.builder()
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
        DumpArticle dumpArticle = DumpArticle.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(today)
                .build();

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getLastUpdate()).thenReturn(today);
        Mockito.when(articleRepository.findByIdGreaterThanOrderById(Mockito.anyInt(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(dbArticle));
        Mockito.when(replacementRepository.findByArticle(dbArticle)).thenReturn(Collections.<Replacement>emptyList());

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessLastUpdateBeforeTimestamp() {
        LocalDate yesterday = LocalDate.now().minusDays(1L);

        DumpArticle dumpArticle = DumpArticle.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .build();

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getLastUpdate()).thenReturn(yesterday);
        Mockito.when(articleRepository.findOne(Mockito.anyInt())).thenReturn(dbArticle);

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
    }

    /* DATABASE TESTS */

    @Test
    public void testProcessNewArticle() {
        DumpArticle dumpArticle = DumpArticle.builder()
                .setId(1)
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .build();

        ArticleReplacement articleReplacement = ArticleReplacement.builder()
                .setType(ReplacementType.MISSPELLING)
                .setSubtype("")
                .build();
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString()))
                .thenReturn(Collections.singletonList(articleReplacement));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        dumpArticleProcessor.finish();

        Mockito.verify(articleRepository).save(Mockito.anyListOf(Article.class));
        Mockito.verify(replacementRepository).save(Mockito.anySetOf(Replacement.class));
    }

    @Test
    public void testProcessExistingArticleWithReplacementChanges() {
        LocalDate now = LocalDate.now();
        DumpArticle dumpArticle = DumpArticle.builder()
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
                .setType(ReplacementType.MISSPELLING)
                .setText("1")
                .build();
        Replacement replacement2 = new Replacement.ReplacementBuilder()
                .setArticle(dbArticle)
                .setType(ReplacementType.MISSPELLING)
                .setText("2")
                .build();
        Mockito.when(replacementRepository.findByArticle(dbArticle)).thenReturn(Arrays.asList(replacement1, replacement2));
        // And the new ones are 2 and 3
        ArticleReplacement articleReplacement2 = ArticleReplacement.builder()
                .setType(ReplacementType.MISSPELLING)
                .setSubtype("2")
                .build();
        ArticleReplacement articleReplacement3 = ArticleReplacement.builder()
                .setType(ReplacementType.MISSPELLING)
                .setSubtype("3")
                .build();
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString()))
                .thenReturn(Arrays.asList(articleReplacement2, articleReplacement3));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        dumpArticleProcessor.finish();

        Mockito.verify(replacementRepository).deleteInBatch(Mockito.anyListOf(Replacement.class));
        Mockito.verify(articleRepository).save(Mockito.anyListOf(Article.class));
        Mockito.verify(replacementRepository).save(Mockito.anySetOf(Replacement.class));
    }

    @Test
    public void testProcessExistingArticleWithNoReplacementChanges() {
        DumpArticle dumpArticle = DumpArticle.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .build();

        // Let's suppose the article exists in DB
        Article dbArticle = Article.builder().build();
        Mockito.when(articleRepository.findOne(Mockito.anyInt())).thenReturn(dbArticle);
        // And it has replacement 1
        Replacement replacement1 = new Replacement.ReplacementBuilder()
                .setArticle(dbArticle)
                .setType(ReplacementType.MISSPELLING)
                .setText("1")
                .build();
        Mockito.when(replacementRepository.findByArticle(dbArticle)).thenReturn(Collections.singletonList(replacement1));
        // And the new one is 1
        ArticleReplacement articleReplacement1 = ArticleReplacement.builder()
                .setType(ReplacementType.MISSPELLING)
                .setSubtype("1")
                .build();
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString()))
                .thenReturn(Collections.singletonList(articleReplacement1));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));

        Mockito.verify(articleRepository, Mockito.times(0)).save(Mockito.any(Article.class));
        Mockito.verify(replacementRepository, Mockito.times(0)).delete(Mockito.any(Replacement.class));
        Mockito.verify(replacementRepository, Mockito.times(0)).save(Mockito.any(Replacement.class));
    }

    @Test
    public void testProcessExistingArticleWithNoReplacementsFound() {
        DumpArticle dumpArticle = DumpArticle.builder()
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
                .setType(ReplacementType.MISSPELLING)
                .setText("1")
                .build();
        Replacement replacement2 = new Replacement.ReplacementBuilder()
                .setArticle(dbArticle)
                .setType(ReplacementType.MISSPELLING)
                .setText("2")
                .build();
        Mockito.when(replacementRepository.findByArticle(dbArticle)).thenReturn(Arrays.asList(replacement1, replacement2));
        // And there are no replacements found
        Mockito.when(replacementFinderService.findReplacements(Mockito.anyString())).thenReturn(Collections.<ArticleReplacement>emptyList());

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        dumpArticleProcessor.finish();

        Mockito.verify(articleService).deleteArticle(Mockito.any(Article.class));
    }

    @Test
    public void testProcessNewArticleAndDeleteObsolete() {
        // New article
        DumpArticle dumpArticle = DumpArticle.builder()
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
        Mockito.verify(articleRepository).save(Mockito.anyListOf(Article.class));
        Mockito.verify(replacementRepository).save(Mockito.anySetOf(Replacement.class));
    }

}
