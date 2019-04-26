package es.bvalero.replacer.article;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.persistence.Article;
import es.bvalero.replacer.persistence.ArticleRepository;
import es.bvalero.replacer.persistence.ReplacementRepository;
import es.bvalero.replacer.persistence.ReplacementType;
import es.bvalero.replacer.wikipedia.WikipediaException;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
        Mockito.when(wikipediaService.getPageContent(Mockito.anyString())).thenReturn(text);

        // Replacement matches
        ArticleReplacement replacement = ArticleReplacement.builder()
                .setStart(0).setText("Z").setType(ReplacementType.MISSPELLING).build();
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

    @Test
    public void testCompare() {
        ArticleReplacement replacement = ArticleReplacement.builder().setStart(1).setText("XXX").build();
        Assert.assertEquals(0, replacement.compareTo(
                ArticleReplacement.builder().setStart(1).setText("XXX").build()));
        Assert.assertEquals(0, replacement.compareTo(
                ArticleReplacement.builder().setStart(1).setText("ZZZ").build()));

        // The matches are sorted in descendant order of apparition
        Assert.assertTrue(replacement.compareTo(
                ArticleReplacement.builder().setStart(0).setText("Z").build()) < 0);
        Assert.assertTrue(replacement.compareTo(
                ArticleReplacement.builder().setStart(2).setText("Z").build()) > 0);
        Assert.assertTrue(replacement.compareTo(
                ArticleReplacement.builder().setStart(1).setText("ZZ").build()) > 0);
    }

    @Test
    @Deprecated
    public void testRemoveNestedMatches() {
        // Sample text: F R E N É T I C A M E N T E
        ArticleReplacement match1 = ArticleReplacement.builder().setStart(1).setText("REN").build(); // 1-3
        ArticleReplacement match2 = ArticleReplacement.builder().setStart(4).setText("ÉTICA").build(); // 4-8
        ArticleReplacement match3 = ArticleReplacement.builder().setStart(7).setText("CAMEN").build(); // 7-11
        ArticleReplacement match4 = ArticleReplacement.builder().setStart(12).setText("TE").build(); // 12-13
        ArticleReplacement match5 = ArticleReplacement.builder().setStart(9).setText("MEN").build(); // 9-11
        ArticleReplacement match6 = ArticleReplacement.builder().setStart(4).setText("ÉT").build(); // 4-5
        ArticleReplacement match7 = ArticleReplacement.builder().setStart(7).setText("CAM").build(); // 7-9

        List<ArticleReplacement> matches = ArticleService.removeNestedReplacements(
                new LinkedList<>(Arrays.asList(match2, match5, match4, match1, match3, match4, match6, match7)));

        Assert.assertEquals(3, matches.size());

        Assert.assertTrue(matches.contains(match1));
        Assert.assertFalse(matches.contains(match2));
        Assert.assertFalse(matches.contains(match3));
        // Matches 2 and 3 are merged
        ArticleReplacement match23 = ArticleReplacement.builder().setStart(4).setText("ÉTICAMEN").build(); // 4-11
        Assert.assertTrue(matches.contains(match23));
        Assert.assertTrue(matches.contains(match4));
        Assert.assertFalse(matches.contains(match5));
        Assert.assertFalse(matches.contains(match6));
        Assert.assertFalse(matches.contains(match7));
    }

}
