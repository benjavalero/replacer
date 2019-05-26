package es.bvalero.replacer.article;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.persistence.Article;
import es.bvalero.replacer.persistence.ArticleRepository;
import es.bvalero.replacer.persistence.ReplacementRepository;
import es.bvalero.replacer.wikipedia.WikipediaException;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ArticleServiceTest {

    @Mock
    private ReplacementFinderService replacementFinderService;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ReplacementRepository replacementRepository;

    @Mock
    private WikipediaService wikipediaService;

    @InjectMocks
    private ArticleService articleService;

    @Before
    public void setUp() {
        articleService = new ArticleService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindRandomArticleWithReplacements()
            throws WikipediaException, InvalidArticleException, UnfoundArticleException {
        String title = "España";
        String text = "Un texto";

        Article randomArticle = Article.builder()
                .setTitle(title)
                .build();
        Mockito.when(replacementRepository.findRandom(Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(randomArticle));
        WikipediaPage page = WikipediaPage.builder().setContent(text).build();
        Mockito.when(wikipediaService.getPageByTitle(Mockito.anyString())).thenReturn(Optional.of(page));

        // Replacement matches
        ArticleReplacement replacement = Mockito.mock(ArticleReplacement.class);
        List<ArticleReplacement> replacementList = new LinkedList<>();
        replacementList.add(replacement);
        Mockito.when(replacementFinderService.findReplacements(text)).thenReturn(replacementList);

        ArticleReview articleData = articleService.findRandomArticleWithReplacements();

        Assert.assertNotNull(articleData);
        Assert.assertEquals(title, articleData.getTitle());
        Assert.assertEquals(text, articleData.getContent());

        List<ArticleReplacement> replacements = articleData.getReplacements();
        Assert.assertEquals(1, replacements.size());
        Assert.assertTrue(replacements.contains(replacement));
    }

    @Test
    public void testSaveArticle() throws WikipediaException {
        String title = "España";
        String text = "Un texto";

        Article articleDb = Article.builder().build();
        Mockito.when(articleRepository.findByTitle(Mockito.anyString())).thenReturn(articleDb);

        OAuth1AccessToken accessToken = Mockito.mock(OAuth1AccessToken.class);
        articleService.saveArticleChanges(title, text, accessToken);

        Mockito.verify(wikipediaService).savePageContent(
                Mockito.eq(title), Mockito.eq(text), Mockito.any(LocalDateTime.class), Mockito.eq(accessToken));
        Mockito.verify(replacementRepository).deleteByArticle(Mockito.any(Article.class));
        Mockito.verify(articleRepository).save(Mockito.any(Article.class));
    }

}
