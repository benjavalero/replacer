package es.bvalero.replacer.article;

import es.bvalero.replacer.article.exception.ErrorExceptionFinder;
import es.bvalero.replacer.misspelling.MisspellingFinder;
import es.bvalero.replacer.utils.RegexMatchType;
import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private IWikipediaFacade wikipediaService;

    @Mock
    private List<ErrorExceptionFinder> errorExceptionFinders;

    @Mock
    private MisspellingFinder misspellingFinder;

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
    public void findRandomArticleWithPotentialErrors() throws Exception {
        Mockito.when(articleRepository.findMaxId()).thenReturn(10);

        String articleTitle = "Un artículo aleatorio";
        Article article = new Article(1, articleTitle);
        Mockito.when(articleRepository.findFirstByIdGreaterThanAndReviewDateNull(Mockito.anyInt()))
                .thenReturn(article);

        String articleContent = "Contenido de Hejemplo con errores.";
        Mockito.when(wikipediaService.getArticleContent(Mockito.anyString()))
                .thenReturn(articleContent);

        ArticleReplacement replacement = new ArticleReplacement();
        replacement.setPosition(13);
        replacement.setOriginalText("Hejemplo");
        replacement.setComment("Sin hache");
        replacement.setType(RegexMatchType.MISSPELLING);
        replacement.setProposedFixes(Collections.singletonList("Hejemplo"));

        // No exceptions found
        Mockito.when(errorExceptionFinders.iterator()).thenReturn(new ArrayList<ErrorExceptionFinder>().iterator());

        Mockito.when(misspellingFinder.findPotentialErrors(articleContent))
                .thenReturn(Collections.singletonList(replacement));
        List<PotentialErrorFinder> finderList = Collections.singletonList((PotentialErrorFinder) misspellingFinder);
        Mockito.when(potentialErrorFinders.iterator()).thenReturn(finderList.iterator());

        ArticleData articleData = articleService.findRandomArticleWithPotentialErrors();
        Assert.assertEquals(articleTitle, articleData.getTitle());
        String replacedContent = articleContent.replace("Hejemplo",
                articleService.getReplacementButtonText(replacement));
        Assert.assertEquals(replacedContent, articleData.getContent());
        Assert.assertFalse(articleData.getFixes().isEmpty());
        Assert.assertEquals("Hejemplo", articleData.getFixes().get(13).getOriginalText());
    }

    @Test
    public void testArticleReplacementSorting() {
        List<ArticleReplacement> repList = new ArrayList<>();
        repList.add(new ArticleReplacement(1, null));
        repList.add(new ArticleReplacement(2, null));
        repList.add(new ArticleReplacement(2, null));
        repList.add(new ArticleReplacement(3, null));

        Collections.sort(repList);
        Assert.assertEquals(4, repList.size());
        Assert.assertEquals(3, repList.get(0).getPosition());
        Assert.assertEquals(2, repList.get(1).getPosition());
        Assert.assertEquals(2, repList.get(2).getPosition());
        Assert.assertEquals(1, repList.get(3).getPosition());
    }

    @Test
    public void testIsRedirectionArticle() {
        ArticleService articleService = new ArticleService();

        Assert.assertTrue(articleService.isRedirectionArticle("xxx #REDIRECCIÓN [[A]] yyy"));
        Assert.assertTrue(articleService.isRedirectionArticle("xxx #redirección [[A]] yyy"));
        Assert.assertTrue(articleService.isRedirectionArticle("xxx #REDIRECT [[A]] yyy"));
        Assert.assertFalse(articleService.isRedirectionArticle("Otro contenido"));
    }

    @Test
    public void testRemoveParagraphsWithoutReplacements() {
        String text = "A\n\nB\n\nC id=\"miss-2\"\n\nD id=\"miss-3\"\n\nE\n\nF id=\"miss-4\"\n\nG\n\nH\n\n";
        String expected = "C id=\"miss-2\"\n<hr>\nD id=\"miss-3\"\n<hr>\nF id=\"miss-4\"";
        ArticleService articleService = new ArticleService();
        Assert.assertEquals(expected, articleService.removeParagraphsWithoutReplacements(text));
    }

    @Test
    public void testTrimText() {
        String text = "Es una casa muy bonita <button>xxx</button> con vistas al mar, solárium en la terraza <button>zzz</button> y minibar en el sótano.";
        String expected = "[...] uy bonita <button>xxx</button> con vista [...] a terraza <button>zzz</button> y minibar [...]";
        ArticleService articleService = new ArticleService();
        Assert.assertEquals(expected, articleService.trimText(text, 10));
    }

}
