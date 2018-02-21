package es.bvalero.replacer.article;

import es.bvalero.replacer.article.exception.ExceptionMatchFinder;
import es.bvalero.replacer.article.finder.PotentialErrorFinder;
import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import es.bvalero.replacer.wikipedia.WikipediaException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private PotentialErrorRepository potentialErrorRepository;

    @Mock
    private IWikipediaFacade wikipediaService;

    @Mock
    private List<ExceptionMatchFinder> exceptionMatchFinders;

    @Mock
    private List<PotentialErrorFinder> potentialErrorFinders;

    @InjectMocks
    private ArticleService articleService;

    @Before
    public void setUp() {
        articleService = new ArticleService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void findRandomArticleWithPotentialErrors() throws WikipediaException {
        Mockito.when(articleRepository.findMaxIdNotReviewed()).thenReturn(10);

        Integer id = 1;
        String title = "España";
        String text = "Un hejemplo con muxos herrores y <XML>.";

        Article randomArticle = new Article(id, title);
        Mockito.when(articleRepository.findFirstByIdGreaterThanAndReviewDateNull(Mockito.anyInt()))
                .thenReturn(randomArticle);

        Mockito.when(wikipediaService.getArticleContent(Mockito.anyString())).thenReturn(text);

        // Exception matches
        ExceptionMatchFinder exceptionMatchFinder = Mockito.mock(ExceptionMatchFinder.class);
        RegexMatch exceptionMatch = new RegexMatch(16, "muxos");
        Mockito.when(exceptionMatchFinder.findExceptionMatches(Mockito.anyString())).thenReturn(Collections.singletonList(exceptionMatch));
        Mockito.when(exceptionMatchFinders.iterator()).thenReturn(Arrays.asList(new ExceptionMatchFinder[]{exceptionMatchFinder}).iterator());

        // Potential error matches
        PotentialErrorFinder potentialErrorFinder = Mockito.mock(PotentialErrorFinder.class);
        ArticleReplacement replacement1 = new ArticleReplacement(3, "hejemplo");
        replacement1.setType(PotentialErrorType.MISSPELLING);
        replacement1.setComment("ejemplo");
        ArticleReplacement replacement2 = new ArticleReplacement(16, "muxos");
        ArticleReplacement replacement3 = new ArticleReplacement(22, "herrores");
        replacement3.setType(PotentialErrorType.MISSPELLING);
        replacement3.setComment("errores");
        Mockito.when(potentialErrorFinder.findPotentialErrors(Mockito.anyString())).thenReturn(Arrays.asList(replacement1, replacement2, replacement3));
        Mockito.when(potentialErrorFinders.iterator()).thenReturn(Arrays.asList(new PotentialErrorFinder[]{potentialErrorFinder}).iterator());

        articleService.setHighlightExceptions(true);

        ArticleData articleData = articleService.findRandomArticleWithPotentialErrors();

        Assert.assertNotNull(articleData);
        Assert.assertEquals(id, articleData.getId());
        Assert.assertEquals(title, articleData.getTitle());

        String replacedContent = "Un " +
                "<button id=\"miss-3\" title=\"ejemplo\" type=\"button\" class=\"miss btn btn-danger\" data-toggle=\"tooltip\" data-placement=\"top\">hejemplo</button>" +
                " con <span class=\"syntax exception\">muxos</span> " +
                "<button id=\"miss-22\" title=\"errores\" type=\"button\" class=\"miss btn btn-danger\" data-toggle=\"tooltip\" data-placement=\"top\">herrores</button>" +
                " y &lt;XML&gt;.";
        Assert.assertEquals(replacedContent, articleData.getContent());

        Collection<ArticleReplacement> potentialErrors = articleData.getFixes().values();
        Assert.assertEquals(2, potentialErrors.size());
        Assert.assertTrue(potentialErrors.contains(replacement1));
        Assert.assertFalse(potentialErrors.contains(replacement2));
        Assert.assertTrue(potentialErrors.contains(replacement3));
    }

    @Test
    public void findRandomArticleByWordHidingParagraphs() throws WikipediaException {
        Mockito.when(potentialErrorRepository.findMaxArticleIdByWordAndNotReviewed(Mockito.anyString())).thenReturn(10);

        Integer id = 1;
        String title = "España";
        String text = "Un hejemplo\n\nX\n\nOtro hejemplo.";

        Article randomArticle = new Article(id, title);
        Mockito.when(potentialErrorRepository
                .findByWordAndIdGreaterThanAndReviewDateNull(Mockito.anyInt(), Mockito.anyString(), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(randomArticle));

        Mockito.when(wikipediaService.getArticleContent(Mockito.anyString())).thenReturn(text);

        // Exception matches
        ExceptionMatchFinder exceptionMatchFinder = Mockito.mock(ExceptionMatchFinder.class);
        Mockito.when(exceptionMatchFinder.findExceptionMatches(Mockito.anyString())).thenReturn(Arrays.asList(new RegexMatch[]{}));
        Mockito.when(exceptionMatchFinders.iterator()).thenReturn(Arrays.asList(new ExceptionMatchFinder[]{exceptionMatchFinder}).iterator());

        // Potential error matches
        PotentialErrorFinder potentialErrorFinder = Mockito.mock(PotentialErrorFinder.class);
        ArticleReplacement replacement1 = new ArticleReplacement(3, "hejemplo");
        ArticleReplacement replacement2 = new ArticleReplacement(21, "hejemplo");
        replacement1.setComment("ejemplo");
        replacement1.setType(PotentialErrorType.MISSPELLING);
        replacement2.setComment("ejemplo");
        replacement2.setType(PotentialErrorType.MISSPELLING);
        Mockito.when(potentialErrorFinder.findPotentialErrors(Mockito.anyString())).thenReturn(Arrays.asList(replacement1, replacement2));
        Mockito.when(potentialErrorFinders.iterator()).thenReturn(Arrays.asList(new PotentialErrorFinder[]{potentialErrorFinder}).iterator());

        articleService.setHideEmptyParagraphs(true);
        ArticleData articleData = articleService.findRandomArticleWithPotentialErrors("xxx");

        String replacedContent = "Un <button id=\"miss-3\" title=\"ejemplo\" type=\"button\" class=\"miss btn btn-danger\" data-toggle=\"tooltip\" data-placement=\"top\">hejemplo</button>"
                + "\n<hr>\n"
                + "Otro <button id=\"miss-21\" title=\"ejemplo\" type=\"button\" class=\"miss btn btn-danger\" data-toggle=\"tooltip\" data-placement=\"top\">hejemplo</button>.";
        Assert.assertNotNull(articleData);
        Assert.assertEquals(replacedContent, articleData.getContent());
    }

}
