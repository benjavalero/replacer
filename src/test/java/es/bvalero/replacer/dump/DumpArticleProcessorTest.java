package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.*;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;

import java.sql.Timestamp;
import java.util.*;

public class DumpArticleProcessorTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private PotentialErrorRepository potentialErrorRepository;

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
        DumpArticle dumpArticle = Mockito.mock(DumpArticle.class);
        Mockito.when(dumpArticle.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        Mockito.when(dumpArticle.getContent()).thenReturn("");
        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testCheckNamespaces() {
        DumpArticle dumpArticle = Mockito.mock(DumpArticle.class);
        Mockito.when(dumpArticle.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        Mockito.when(dumpArticle.getContent()).thenReturn("");

        DumpArticle dumpAnnex = Mockito.mock(DumpArticle.class);
        Mockito.when(dumpAnnex.getNamespace()).thenReturn(WikipediaNamespace.ANNEX);
        Mockito.when(dumpAnnex.getContent()).thenReturn("");

        DumpArticle dumpCategory = Mockito.mock(DumpArticle.class);
        Mockito.when(dumpCategory.getNamespace()).thenReturn(WikipediaNamespace.CATEGORY);

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpAnnex));
        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpCategory));
    }

    @Test
    public void testProcessRedirection() {
        DumpArticle dumpArticle = Mockito.mock(DumpArticle.class);
        Mockito.when(dumpArticle.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        Mockito.when(dumpArticle.getContent()).thenReturn("#REDIRECT xxx");
        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessReviewedAfterTimestamp() {
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);

        DumpArticle dumpArticle = Mockito.mock(DumpArticle.class);
        Mockito.when(dumpArticle.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        Mockito.when(dumpArticle.getContent()).thenReturn("");
        Mockito.when(dumpArticle.getTimestamp()).thenReturn(new Date(yesterday.getTimeInMillis()));

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getReviewDate()).thenReturn(new Timestamp(today.getTimeInMillis()));
        Mockito.when(articleRepository.findByIdGreaterThanOrderById(Mockito.anyInt(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(dbArticle));

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessReviewedAfterTimestampForced() {
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);

        DumpArticle dumpArticle = Mockito.mock(DumpArticle.class);
        Mockito.when(dumpArticle.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        Mockito.when(dumpArticle.getContent()).thenReturn("");
        Mockito.when(dumpArticle.getTimestamp()).thenReturn(new Date(yesterday.getTimeInMillis()));

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getReviewDate()).thenReturn(new Timestamp(today.getTimeInMillis()));
        Mockito.when(articleRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(dbArticle));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, true));
    }

    @Test
    public void testProcessReviewedWhenTimestamp() {
        GregorianCalendar today = new GregorianCalendar();

        DumpArticle dumpArticle = Mockito.mock(DumpArticle.class);
        Mockito.when(dumpArticle.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        Mockito.when(dumpArticle.getContent()).thenReturn("");
        Mockito.when(dumpArticle.getTimestamp()).thenReturn(new Date(today.getTimeInMillis()));

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getReviewDate()).thenReturn(new Timestamp(today.getTimeInMillis()));
        Mockito.when(articleRepository.findByIdGreaterThanOrderById(Mockito.anyInt(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(dbArticle));

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessReviewedBeforeTimestamp() {
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);

        DumpArticle dumpArticle = Mockito.mock(DumpArticle.class);
        Mockito.when(dumpArticle.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        Mockito.when(dumpArticle.getContent()).thenReturn("");
        Mockito.when(dumpArticle.getTimestamp()).thenReturn(new Date(today.getTimeInMillis()));

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getReviewDate()).thenReturn(new Timestamp(yesterday.getTimeInMillis()));
        Mockito.when(articleRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(dbArticle));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessAddedAfterTimestamp() {
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);

        DumpArticle dumpArticle = Mockito.mock(DumpArticle.class);
        Mockito.when(dumpArticle.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        Mockito.when(dumpArticle.getContent()).thenReturn("");
        Mockito.when(dumpArticle.getTimestamp()).thenReturn(new Date(yesterday.getTimeInMillis()));

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getAdditionDate()).thenReturn(new Timestamp(today.getTimeInMillis()));
        Mockito.when(articleRepository.findByIdGreaterThanOrderById(Mockito.anyInt(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(dbArticle));

        Assert.assertFalse(dumpArticleProcessor.processArticle(dumpArticle));
    }

    @Test
    public void testProcessAddedAfterTimestampForced() {
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);

        DumpArticle dumpArticle = Mockito.mock(DumpArticle.class);
        Mockito.when(dumpArticle.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        Mockito.when(dumpArticle.getContent()).thenReturn("");
        Mockito.when(dumpArticle.getTimestamp()).thenReturn(new Date(yesterday.getTimeInMillis()));

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getAdditionDate()).thenReturn(new Timestamp(today.getTimeInMillis()));
        Mockito.when(articleRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(dbArticle));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle, true));
    }

    @Test
    public void testProcessAddedBeforeTimestamp() {
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);

        DumpArticle dumpArticle = Mockito.mock(DumpArticle.class);
        Mockito.when(dumpArticle.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        Mockito.when(dumpArticle.getContent()).thenReturn("");
        Mockito.when(dumpArticle.getTimestamp()).thenReturn(new Date(today.getTimeInMillis()));

        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(dbArticle.getAdditionDate()).thenReturn(new Timestamp(yesterday.getTimeInMillis()));
        Mockito.when(articleRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(dbArticle));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
    }

    /* DATABASE TESTS */

    @Test
    public void testProcessNewArticle() {
        DumpArticle dumpArticle = Mockito.mock(DumpArticle.class);
        Mockito.when(dumpArticle.getId()).thenReturn(1);
        Mockito.when(dumpArticle.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        Mockito.when(dumpArticle.getContent()).thenReturn("");

        ArticleReplacement articleReplacement = Mockito.mock(ArticleReplacement.class);
        Mockito.when(articleService.findPotentialErrorsIgnoringExceptions(Mockito.anyString()))
                .thenReturn(Collections.singletonList(articleReplacement));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        dumpArticleProcessor.finish();

        Mockito.verify(articleRepository).saveAll(Mockito.anyList());
        Mockito.verify(potentialErrorRepository).saveAll(Mockito.anyList());
    }

    @Test
    public void testProcessExistingArticleWithReplacementChanges() {
        DumpArticle dumpArticle = Mockito.mock(DumpArticle.class);
        Mockito.when(dumpArticle.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        Mockito.when(dumpArticle.getContent()).thenReturn("");

        // Let's suppose the article exists in DB
        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(articleRepository.findByIdGreaterThanOrderById(Mockito.anyInt(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(dbArticle));
        // And it has replacements 1 and 2
        PotentialError replacement1 = new PotentialError(dbArticle, PotentialErrorType.MISSPELLING, "1");
        PotentialError replacement2 = new PotentialError(dbArticle, PotentialErrorType.MISSPELLING, "2");
        Mockito.when(potentialErrorRepository.findByArticle(dbArticle)).thenReturn(Arrays.asList(replacement1, replacement2));
        // And the new ones are 2 and 3
        ArticleReplacement articleReplacement2 = Mockito.mock(ArticleReplacement.class);
        Mockito.when(articleReplacement2.getType()).thenReturn(PotentialErrorType.MISSPELLING);
        Mockito.when(articleReplacement2.getSubtype()).thenReturn("2");
        ArticleReplacement articleReplacement3 = Mockito.mock(ArticleReplacement.class);
        Mockito.when(articleReplacement3.getType()).thenReturn(PotentialErrorType.MISSPELLING);
        Mockito.when(articleReplacement3.getSubtype()).thenReturn("3");
        Mockito.when(articleService.findPotentialErrorsIgnoringExceptions(Mockito.anyString()))
                .thenReturn(Arrays.asList(articleReplacement2, articleReplacement3));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        dumpArticleProcessor.finish();

        Mockito.verify(potentialErrorRepository).deleteInBatch(Mockito.anyList());
        Mockito.verify(articleRepository).saveAll(Mockito.anyList());
        Mockito.verify(potentialErrorRepository).saveAll(Mockito.anyList());
    }

    @Test
    public void testProcessExistingArticleWithNoReplacementChanges() {
        DumpArticle dumpArticle = Mockito.mock(DumpArticle.class);
        Mockito.when(dumpArticle.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        Mockito.when(dumpArticle.getContent()).thenReturn("");

        // Let's suppose the article exists in DB
        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(articleRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(dbArticle));
        // And it has replacement 1
        PotentialError replacement1 = new PotentialError(dbArticle, PotentialErrorType.MISSPELLING, "1");
        Mockito.when(potentialErrorRepository.findByArticle(dbArticle)).thenReturn(Collections.singletonList(replacement1));
        // And the new one is 1
        ArticleReplacement articleReplacement1 = Mockito.mock(ArticleReplacement.class);
        Mockito.when(articleReplacement1.getType()).thenReturn(PotentialErrorType.MISSPELLING);
        Mockito.when(articleReplacement1.getSubtype()).thenReturn("1");
        Mockito.when(articleService.findPotentialErrorsIgnoringExceptions(Mockito.anyString()))
                .thenReturn(Collections.singletonList(articleReplacement1));

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));

        Mockito.verify(articleRepository, Mockito.times(0)).save(Mockito.any(Article.class));
        Mockito.verify(potentialErrorRepository, Mockito.times(0)).delete(Mockito.any(PotentialError.class));
        Mockito.verify(potentialErrorRepository, Mockito.times(0)).save(Mockito.any(PotentialError.class));
    }

    @Test
    public void testProcessExistingArticleWithNoReplacementsFound() {
        DumpArticle dumpArticle = Mockito.mock(DumpArticle.class);
        Mockito.when(dumpArticle.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        Mockito.when(dumpArticle.getContent()).thenReturn("");

        // Let's suppose the article exists in DB
        Article dbArticle = Mockito.mock(Article.class);
        Mockito.when(articleRepository.findByIdGreaterThanOrderById(Mockito.anyInt(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(dbArticle));
        // And it has replacements 1 and 2
        PotentialError replacement1 = new PotentialError(dbArticle, PotentialErrorType.MISSPELLING, "1");
        PotentialError replacement2 = new PotentialError(dbArticle, PotentialErrorType.MISSPELLING, "2");
        Mockito.when(potentialErrorRepository.findByArticle(dbArticle)).thenReturn(Arrays.asList(replacement1, replacement2));
        // And there are no replacements found
        Mockito.when(articleService.findPotentialErrorsIgnoringExceptions(Mockito.anyString())).thenReturn(Collections.emptyList());

        Assert.assertTrue(dumpArticleProcessor.processArticle(dumpArticle));
        dumpArticleProcessor.finish();

        Mockito.verify(potentialErrorRepository).deleteByArticle(dbArticle);
        Mockito.verify(articleRepository).deleteInBatch(Mockito.anyList());
    }

    @Test
    public void testProcessNewArticleAndDeleteObsolete() {
        // New article
        DumpArticle dumpArticle = Mockito.mock(DumpArticle.class);
        Mockito.when(dumpArticle.getId()).thenReturn(2);
        Mockito.when(dumpArticle.getNamespace()).thenReturn(WikipediaNamespace.ARTICLE);
        Mockito.when(dumpArticle.getContent()).thenReturn("");

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
        Mockito.verify(potentialErrorRepository).saveAll(Mockito.anyList());
    }

}
