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
        Mockito.when(articleService.findPotentialErrorsAndExceptions(Mockito.anyString()))
                .thenReturn(Collections.singletonList(new RegexMatch()));
        dumpProcessor.processArticle(null, "", 0, null, null);

        Mockito.verify(articleRepository).save(Mockito.any(Article.class));
    }

    @Test
    public void testProcessOnlyArticlesAndAnnexes() {
        dumpProcessor.processArticle(null, "", 1, null, null);
        Mockito.verify(articleRepository, Mockito.times(0)).findOne(Mockito.anyInt());
        Mockito.verify(articleService, Mockito.times(0)).findPotentialErrorsAndExceptions(Mockito.anyString());
        Mockito.verify(articleRepository, Mockito.times(0)).save(Mockito.any(Article.class));
    }

    @Test
    public void testNotProcessArticlesReviewedAfter() {
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);

        Article articleDb = new Article(1, "");
        articleDb.setReviewDate(new Timestamp(today.getTimeInMillis()));
        Mockito.when(articleRepository.findOne(Mockito.anyInt())).thenReturn(articleDb);

        dumpProcessor.processArticle(1, "", 0, null, new Timestamp(yesterday.getTimeInMillis()));
        Mockito.verify(articleRepository).findOne(Mockito.anyInt());
        Mockito.verify(articleService, Mockito.times(0)).findPotentialErrorsAndExceptions(Mockito.anyString());
        Mockito.verify(articleRepository, Mockito.times(0)).save(Mockito.any(Article.class));
    }

    @Test
    public void testNotProcessArticlesReviewedEquals() {
        GregorianCalendar today = new GregorianCalendar();

        Article articleDb = new Article(1, "");
        articleDb.setReviewDate(new Timestamp(today.getTimeInMillis()));
        Mockito.when(articleRepository.findOne(Mockito.anyInt())).thenReturn(articleDb);

        dumpProcessor.processArticle(1, "", 0, null, new Timestamp(today.getTimeInMillis()));
        Mockito.verify(articleRepository).findOne(Mockito.anyInt());
        Mockito.verify(articleService, Mockito.times(0)).findPotentialErrorsAndExceptions(Mockito.anyString());
        Mockito.verify(articleRepository, Mockito.times(0)).save(Mockito.any(Article.class));
    }

    @Test
    public void testNotProcessArticlesAddedAfter() {
        GregorianCalendar today = new GregorianCalendar();
        GregorianCalendar yesterday = new GregorianCalendar();
        yesterday.add(GregorianCalendar.DATE, -1);

        Article articleDb = new Article(1, "");
        articleDb.setAdditionDate(new Timestamp(today.getTimeInMillis()));
        Mockito.when(articleRepository.findOne(Mockito.anyInt())).thenReturn(articleDb);

        dumpProcessor.processArticle(1, "", 0, null, new Timestamp(yesterday.getTimeInMillis()));
        Mockito.verify(articleRepository).findOne(Mockito.anyInt());
        Mockito.verify(articleService, Mockito.times(0)).findPotentialErrorsAndExceptions(Mockito.anyString());
        Mockito.verify(articleRepository, Mockito.times(0)).save(Mockito.any(Article.class));
    }

}
