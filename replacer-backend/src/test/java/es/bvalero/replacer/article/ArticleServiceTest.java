package es.bvalero.replacer.article;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ArticleServiceTest {

    @Mock
    private ReplacementFinderService replacementFinderService;

    @Mock
    private ReplacementRepository replacementRepository;

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private ArticleIndexService articleIndexService;

    @InjectMocks
    private ArticleService articleService;

    @Before
    public void setUp() {
        articleService = new ArticleService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindArticleReview() throws WikipediaException {
        String title = "España";
        String text = "Un texto";

        WikipediaPage page = WikipediaPage.builder().title(title).content(text).lastUpdate(LocalDateTime.now()).build();
        Mockito.when(wikipediaService.getPageById(Mockito.anyInt())).thenReturn(Optional.of(page));

        // Replacement matches
        ArticleReplacement replacement = Mockito.mock(ArticleReplacement.class);
        Mockito.when(replacementFinderService.findReplacements(text)).thenReturn(Collections.singletonList(replacement));

        Optional<ArticleReview> articleData = articleService.findArticleReviewById(1, null, null);

        Assert.assertTrue(articleData.isPresent());
        Assert.assertEquals(title, articleData.get().getTitle());
        Assert.assertEquals(text, articleData.get().getContent());

        List<ArticleReplacement> replacements = articleData.get().getReplacements();
        Assert.assertEquals(1, replacements.size());
        Assert.assertTrue(replacements.contains(replacement));
    }

    @Test
    public void testSaveArticle() throws WikipediaException {
        int articleId = 1;
        String text = "Un texto";

        WikipediaPage page = WikipediaPage.builder().build();
        Mockito.when(wikipediaService.getPageById(articleId)).thenReturn(Optional.of(page));

        Replacement replacement = new Replacement(1, "", "", 1);
        Mockito.when(replacementRepository.findByArticleIdAndReviewerIsNull(Mockito.anyInt()))
                .thenReturn(Collections.singletonList(replacement));

        OAuth1AccessToken accessToken = Mockito.mock(OAuth1AccessToken.class);

        articleService.saveArticleChanges(articleId, text, null, null, "x", "x", accessToken);

        Mockito.verify(wikipediaService).savePageContent(
                Mockito.eq(articleId), Mockito.eq(text), Mockito.anyString(), Mockito.eq(accessToken));
        Mockito.verify(articleIndexService).reviewReplacement(Mockito.any(Replacement.class), Mockito.anyString(), Mockito.anyBoolean());
    }

}
