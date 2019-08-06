package es.bvalero.replacer.article;

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

        Optional<ArticleReview> articleData = articleService.findArticleReview(1, null, null, null);

        Assert.assertTrue(articleData.isPresent());
        Assert.assertEquals(title, articleData.get().getTitle());
        Assert.assertEquals(text, articleData.get().getContent());

        List<ArticleReplacement> replacements = articleData.get().getReplacements();
        Assert.assertEquals(1, replacements.size());
        Assert.assertTrue(replacements.contains(replacement));
    }

}
