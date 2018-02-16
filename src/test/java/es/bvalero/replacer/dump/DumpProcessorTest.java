package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.Article;
import es.bvalero.replacer.article.ArticleRepository;
import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.utils.RegexMatch;
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

    private DumpArticle dumpArticle;

    @Before
    public void setUp() {
        dumpProcessor = new DumpProcessor();
        MockitoAnnotations.initMocks(this);

        dumpArticle = new DumpArticle();
        dumpArticle.setNamespace(0);
        dumpArticle.setContent("");
    }

    @Test
    public void testProcess() {
        Mockito.when(articleService.findPotentialErrorsAndExceptions(Mockito.anyString()))
                .thenReturn(Collections.singletonList(new RegexMatch()));

        dumpProcessor.processArticle(dumpArticle);

        Mockito.verify(articleRepository, Mockito.times(1)).findOne(Mockito.anyInt());
        Mockito.verify(articleService, Mockito.times(1)).findPotentialErrorsAndExceptions(Mockito.anyString());
        Mockito.verify(articleRepository, Mockito.times(0)).delete(Mockito.any(Article.class));
        Mockito.verify(articleRepository, Mockito.times(1)).save(Mockito.any(Article.class));
    }

    @Test
    public void testProcessExistingArticle() {
        dumpArticle.setTimestamp(new Timestamp(new Date().getTime()));

        Mockito.when(articleService.findPotentialErrorsAndExceptions(Mockito.anyString()))
                .thenReturn(Collections.singletonList(new RegexMatch()));

        Article articleDb = new Article(1, "");
        articleDb.setAdditionDate(new Timestamp(new Date().getTime()));
        Mockito.when(articleRepository.findOne(Mockito.anyInt())).thenReturn(articleDb);

        dumpProcessor.processArticle(dumpArticle);

        Mockito.verify(articleRepository, Mockito.times(1)).findOne(Mockito.anyInt());
        Mockito.verify(articleService, Mockito.times(1)).findPotentialErrorsAndExceptions(Mockito.anyString());
        Mockito.verify(articleRepository, Mockito.times(0)).delete(Mockito.any(Article.class));
        Mockito.verify(articleRepository, Mockito.times(1)).save(Mockito.any(Article.class));
    }

    @Test
    public void testProcessArticleWithoutPotentialErrors() {
        dumpArticle.setTimestamp(new Timestamp(new Date().getTime()));

        Article articleDb = new Article(1, "");
        articleDb.setAdditionDate(new Timestamp(new Date().getTime()));
        Mockito.when(articleRepository.findOne(Mockito.anyInt())).thenReturn(articleDb);

        dumpProcessor.processArticle(dumpArticle);

        Mockito.verify(articleRepository, Mockito.times(1)).findOne(Mockito.anyInt());
        Mockito.verify(articleService, Mockito.times(1)).findPotentialErrorsAndExceptions(Mockito.anyString());
        Mockito.verify(articleRepository, Mockito.times(1)).delete(Mockito.any(Article.class));
        Mockito.verify(articleRepository, Mockito.times(0)).save(Mockito.any(Article.class));
    }

    @Test
    public void testProcessOnlyArticlesAndAnnexes() {
        dumpArticle.setNamespace(1);
        dumpProcessor.processArticle(dumpArticle);
        Mockito.verify(articleRepository, Mockito.times(0)).findOne(Mockito.anyInt());
    }

    @Test
    public void testNotProcessRedirects() {
        Mockito.when(articleService.isRedirectionArticle(Mockito.anyString())).thenReturn(true);
        dumpProcessor.processArticle(dumpArticle);
        Mockito.verify(articleRepository, Mockito.times(0)).findOne(Mockito.anyInt());
    }

    @Test
    public void testSkipArticleReviewedAfterDumpTimestamp() {
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);
        dumpArticle.setTimestamp(new Timestamp(yesterday.getTimeInMillis()));

        Article articleDb = new Article(1, "");
        articleDb.setReviewDate(new Timestamp(today.getTimeInMillis()));
        Mockito.when(articleRepository.findOne(Mockito.anyInt())).thenReturn(articleDb);

        dumpProcessor.processArticle(dumpArticle);

        Mockito.verify(articleRepository, Mockito.times(1)).findOne(Mockito.anyInt());
        Mockito.verify(articleService, Mockito.times(0)).findPotentialErrorsAndExceptions(Mockito.anyString());
        Mockito.verify(articleRepository, Mockito.times(0)).save(Mockito.any(Article.class));
    }

    @Test
    public void testSkipArticleReviewedWhenDumpTimestamp() {
        GregorianCalendar today = new GregorianCalendar();
        dumpArticle.setTimestamp(new Timestamp(today.getTimeInMillis()));

        Article articleDb = new Article(1, "");
        articleDb.setReviewDate(new Timestamp(today.getTimeInMillis()));
        Mockito.when(articleRepository.findOne(Mockito.anyInt())).thenReturn(articleDb);

        dumpProcessor.processArticle(dumpArticle);

        Mockito.verify(articleRepository, Mockito.times(1)).findOne(Mockito.anyInt());
        Mockito.verify(articleService, Mockito.times(0)).findPotentialErrorsAndExceptions(Mockito.anyString());
        Mockito.verify(articleRepository, Mockito.times(0)).save(Mockito.any(Article.class));
    }

    @Test
    public void testSkipArticleAddedAfter() {
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);
        dumpArticle.setTimestamp(new Timestamp(yesterday.getTimeInMillis()));

        Article articleDb = new Article(1, "");
        articleDb.setAdditionDate(new Timestamp(today.getTimeInMillis()));
        Mockito.when(articleRepository.findOne(Mockito.anyInt())).thenReturn(articleDb);

        dumpProcessor.processArticle(dumpArticle);

        Mockito.verify(articleRepository, Mockito.times(1)).findOne(Mockito.anyInt());
        Mockito.verify(articleService, Mockito.times(0)).findPotentialErrorsAndExceptions(Mockito.anyString());
        Mockito.verify(articleRepository, Mockito.times(0)).save(Mockito.any(Article.class));
    }

}
