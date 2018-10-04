package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.ArticleService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class DumpArticleProcessorTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ReplacementRepository replacementRepository;

    @Mock
    private ArticleService articleService;

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
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1L);

        DumpArticle dumpArticle = DumpArticle.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(yesterday)
                .build();

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getReviewDate()).thenReturn(today);
        Mockito.when(articleRepository.findByIdGreaterThanOrderById(Mockito.anyInt(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(dbArticle));

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessReviewedAfterTimestampForced() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1L);

        DumpArticle dumpArticle = DumpArticle.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(yesterday)
                .build();

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getReviewDate()).thenReturn(today);
        Mockito.when(articleRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(dbArticle));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, true));
    }

    @Test
    public void testProcessReviewedWhenTimestamp() {
        DumpArticle dumpArticle = DumpArticle.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(LocalDateTime.now())
                .build();

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getReviewDate()).thenReturn(LocalDateTime.now());
        Mockito.when(articleRepository.findByIdGreaterThanOrderById(Mockito.anyInt(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(dbArticle));

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessReviewedBeforeTimestamp() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1L);

        DumpArticle dumpArticle = DumpArticle.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .build();

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getReviewDate()).thenReturn(yesterday);
        Mockito.when(articleRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(dbArticle));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessAddedAfterTimestamp() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1L);

        DumpArticle dumpArticle = DumpArticle.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(yesterday)
                .build();

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getAdditionDate()).thenReturn(today);
        Mockito.when(articleRepository.findByIdGreaterThanOrderById(Mockito.anyInt(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(dbArticle));

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessAddedAfterTimestampForced() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1L);

        DumpArticle dumpArticle = DumpArticle.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(yesterday)
                .build();

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getAdditionDate()).thenReturn(today);
        Mockito.when(articleRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(dbArticle));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, true));
    }

    @Test
    public void testProcessAddedBeforeTimestamp() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1L);

        DumpArticle dumpArticle = DumpArticle.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .build();

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getAdditionDate()).thenReturn(yesterday);
        Mockito.when(articleRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(dbArticle));

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

        ArticleReplacement articleReplacement = Mockito.mock(ArticleReplacement.class);
        Mockito.when(articleService.findPotentialErrorsIgnoringExceptions(Mockito.anyString()))
                .thenReturn(Collections.singletonList(articleReplacement));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        dumpArticleProcessor.finish();

        Mockito.verify(articleRepository).saveAll(Mockito.anyList());
        Mockito.verify(replacementRepository).saveAll(Mockito.anyList());
    }

    @Test
    public void testProcessExistingArticleWithReplacementChanges() {
        DumpArticle dumpArticle = DumpArticle.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .setTimestamp(LocalDateTime.now())
                .build();

        // Let's suppose the article exists in DB
        Article dbArticle = Article.builder().setTitle("").build();
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
        ArticleReplacement articleReplacement2 = Mockito.mock(ArticleReplacement.class);
        Mockito.when(articleReplacement2.getType()).thenReturn(ReplacementType.MISSPELLING);
        Mockito.when(articleReplacement2.getSubtype()).thenReturn("2");
        ArticleReplacement articleReplacement3 = Mockito.mock(ArticleReplacement.class);
        Mockito.when(articleReplacement3.getType()).thenReturn(ReplacementType.MISSPELLING);
        Mockito.when(articleReplacement3.getSubtype()).thenReturn("3");
        Mockito.when(articleService.findPotentialErrorsIgnoringExceptions(Mockito.anyString()))
                .thenReturn(Arrays.asList(articleReplacement2, articleReplacement3));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        dumpArticleProcessor.finish();

        Mockito.verify(replacementRepository).deleteInBatch(Mockito.anyList());
        Mockito.verify(articleRepository).saveAll(Mockito.anyList());
        Mockito.verify(replacementRepository).saveAll(Mockito.anyList());
    }

    @Test
    public void testProcessExistingArticleWithNoReplacementChanges() {
        DumpArticle dumpArticle = DumpArticle.builder()
                .setNamespace(WikipediaNamespace.ARTICLE)
                .setContent("")
                .build();

        // Let's suppose the article exists in DB
        Article dbArticle = Article.builder().build();
        Mockito.when(articleRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(dbArticle));
        // And it has replacement 1
        Replacement replacement1 = new Replacement.ReplacementBuilder()
                .setArticle(dbArticle)
                .setType(ReplacementType.MISSPELLING)
                .setText("1")
                .build();
        Mockito.when(replacementRepository.findByArticle(dbArticle)).thenReturn(Collections.singletonList(replacement1));
        // And the new one is 1
        ArticleReplacement articleReplacement1 = Mockito.mock(ArticleReplacement.class);
        Mockito.when(articleReplacement1.getType()).thenReturn(ReplacementType.MISSPELLING);
        Mockito.when(articleReplacement1.getSubtype()).thenReturn("1");
        Mockito.when(articleService.findPotentialErrorsIgnoringExceptions(Mockito.anyString()))
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
                .setTimestamp(LocalDateTime.now())
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
        Mockito.when(articleService.findPotentialErrorsIgnoringExceptions(Mockito.anyString())).thenReturn(Collections.emptyList());

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        dumpArticleProcessor.finish();

        Mockito.verify(replacementRepository).deleteByArticle(dbArticle);
        Mockito.verify(articleRepository).deleteInBatch(Mockito.anyList());
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

        ArticleReplacement articleReplacement = Mockito.mock(ArticleReplacement.class);
        Mockito.when(articleService.findPotentialErrorsIgnoringExceptions(Mockito.anyString()))
                .thenReturn(Collections.singletonList(articleReplacement));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        dumpArticleProcessor.finish();

        Mockito.verify(articleRepository).deleteInBatch(Mockito.anyList());
        Mockito.verify(articleRepository).saveAll(Mockito.anyList());
        Mockito.verify(replacementRepository).saveAll(Mockito.anyList());
    }

}
