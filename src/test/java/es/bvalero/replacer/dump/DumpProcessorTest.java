package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.Article;
import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.ArticleRepository;
import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

public class DumpProcessorTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleService articleService;

    @InjectMocks
    private DumpProcessor dumpProcessor;

    @Before
    public void setUp() {
        dumpProcessor = new DumpProcessor();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcess() {
        DumpArticle dumpArticle = new DumpArticle(1, "", WikipediaNamespace.ARTICLE, null, "");

        Mockito.when(articleService.findPotentialErrorsIgnoringExceptions(Mockito.anyString()))
                .thenReturn(Collections.singletonList(new ArticleReplacement()));

        dumpProcessor.processArticle(dumpArticle, false);

        Mockito.verify(articleRepository, Mockito.times(1)).findFirst1000ByIdGreaterThanOrderById(Mockito.anyInt());
        Mockito.verify(articleService, Mockito.times(1)).findPotentialErrorsIgnoringExceptions(Mockito.anyString());
        Mockito.verify(articleRepository, Mockito.times(0)).delete(Mockito.any(Article.class));
        Mockito.verify(articleRepository, Mockito.times(1)).save(Mockito.any(Article.class));
    }

    @Test
    public void testProcessExistingArticle() {
        Timestamp today = new Timestamp(new Date().getTime());
        DumpArticle dumpArticle = new DumpArticle(1, "", WikipediaNamespace.ARTICLE, today, "");

        Mockito.when(articleService.findPotentialErrorsIgnoringExceptions(Mockito.anyString()))
                .thenReturn(Collections.singletonList(new ArticleReplacement()));

        Article articleDb = new Article(1, "");
        articleDb.setAdditionDate(new Timestamp(new Date().getTime()));
        Mockito.when(articleRepository.findOne(Mockito.anyInt())).thenReturn(articleDb);

        dumpProcessor.processArticle(dumpArticle, false);

        Mockito.verify(articleRepository, Mockito.times(1)).findFirst1000ByIdGreaterThanOrderById(Mockito.anyInt());
        Mockito.verify(articleService, Mockito.times(1)).findPotentialErrorsIgnoringExceptions(Mockito.anyString());
        Mockito.verify(articleRepository, Mockito.times(0)).delete(Mockito.any(Article.class));
        Mockito.verify(articleRepository, Mockito.times(1)).save(Mockito.any(Article.class));
    }

    @Test
    public void testProcessArticleWithoutPotentialErrors() {
        Timestamp today = new Timestamp(new Date().getTime());
        DumpArticle dumpArticle = new DumpArticle(1, "", WikipediaNamespace.ARTICLE, today, "");

        Article articleDb = new Article(1, "");
        articleDb.setAdditionDate(new Timestamp(new Date().getTime()));
        Mockito.when(articleRepository.findFirst1000ByIdGreaterThanOrderById(Mockito.anyInt()))
                .thenReturn(Collections.singletonList(articleDb));

        dumpProcessor.processArticle(dumpArticle, false);

        Mockito.verify(articleRepository, Mockito.times(1)).findFirst1000ByIdGreaterThanOrderById(Mockito.anyInt());
        Mockito.verify(articleService, Mockito.times(1)).findPotentialErrorsIgnoringExceptions(Mockito.anyString());
        Mockito.verify(articleRepository, Mockito.times(1)).delete(Mockito.any(Article.class));
        Mockito.verify(articleRepository, Mockito.times(0)).save(Mockito.any(Article.class));
    }

    @Test
    public void testSkipArticleReviewedAfterDumpTimestamp() {
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);
        Timestamp timestampYesterday = new Timestamp(yesterday.getTimeInMillis());
        DumpArticle dumpArticle = new DumpArticle(1, "", WikipediaNamespace.ARTICLE, timestampYesterday, "");

        Article articleDb = new Article(1, "");
        articleDb.setReviewDate(new Timestamp(today.getTimeInMillis()));
        Mockito.when(articleRepository.findFirst1000ByIdGreaterThanOrderById(Mockito.anyInt()))
                .thenReturn(Collections.singletonList(articleDb));

        dumpProcessor.processArticle(dumpArticle, false);

        Mockito.verify(articleRepository, Mockito.times(1)).findFirst1000ByIdGreaterThanOrderById(Mockito.anyInt());
        Mockito.verify(articleService, Mockito.times(0)).findPotentialErrorsIgnoringExceptions(Mockito.anyString());
        Mockito.verify(articleRepository, Mockito.times(0)).save(Mockito.any(Article.class));
    }

    @Test
    public void testSkipArticleReviewedWhenDumpTimestamp() {
        Timestamp today = new Timestamp(new Date().getTime());
        DumpArticle dumpArticle = new DumpArticle(1, "", WikipediaNamespace.ARTICLE, today, "");

        Article articleDb = new Article(1, "");
        articleDb.setReviewDate(today);
        Mockito.when(articleRepository.findFirst1000ByIdGreaterThanOrderById(Mockito.anyInt()))
                .thenReturn(Collections.singletonList(articleDb));

        dumpProcessor.processArticle(dumpArticle, false);

        Mockito.verify(articleRepository, Mockito.times(1)).findFirst1000ByIdGreaterThanOrderById(Mockito.anyInt());
        Mockito.verify(articleService, Mockito.times(0)).findPotentialErrorsIgnoringExceptions(Mockito.anyString());
        Mockito.verify(articleRepository, Mockito.times(0)).save(Mockito.any(Article.class));
    }

    @Test
    public void testSkipArticleAddedAfter() {
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);
        Timestamp timestampYesterday = new Timestamp(yesterday.getTimeInMillis());
        DumpArticle dumpArticle = new DumpArticle(1, "", WikipediaNamespace.ARTICLE, timestampYesterday, "");

        Article articleDb = new Article(1, "");
        articleDb.setAdditionDate(new Timestamp(today.getTimeInMillis()));
        Mockito.when(articleRepository.findFirst1000ByIdGreaterThanOrderById(Mockito.anyInt()))
                .thenReturn(Collections.singletonList(articleDb));

        dumpProcessor.processArticle(dumpArticle, false);

        Mockito.verify(articleRepository, Mockito.times(1)).findFirst1000ByIdGreaterThanOrderById(Mockito.anyInt());
        Mockito.verify(articleService, Mockito.times(0)).findPotentialErrorsIgnoringExceptions(Mockito.anyString());
        Mockito.verify(articleRepository, Mockito.times(0)).save(Mockito.any(Article.class));
    }

    @Test
    public void testForceArticleAddedAfter() {
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);
        Timestamp timestampYesterday = new Timestamp(yesterday.getTimeInMillis());
        DumpArticle dumpArticle = new DumpArticle(1, "", WikipediaNamespace.ARTICLE, timestampYesterday, "");

        Mockito.when(articleService.findPotentialErrorsIgnoringExceptions(Mockito.anyString()))
                .thenReturn(Collections.singletonList(new ArticleReplacement()));

        Article articleDb = new Article(1, "");
        articleDb.setAdditionDate(new Timestamp(today.getTimeInMillis()));
        Mockito.when(articleRepository.findOne(Mockito.anyInt())).thenReturn(articleDb);

        dumpProcessor.processArticle(dumpArticle, true);

        Mockito.verify(articleRepository, Mockito.times(1)).findFirst1000ByIdGreaterThanOrderById(Mockito.anyInt());
        Mockito.verify(articleService, Mockito.times(1)).findPotentialErrorsIgnoringExceptions(Mockito.anyString());
        Mockito.verify(articleRepository, Mockito.times(0)).delete(Mockito.any(Article.class));
        Mockito.verify(articleRepository, Mockito.times(1)).save(Mockito.any(Article.class));
    }

}
