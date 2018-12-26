package es.bvalero.replacer.article;

import es.bvalero.replacer.persistence.Article;
import es.bvalero.replacer.persistence.ArticleRepository;
import es.bvalero.replacer.persistence.ReplacementRepository;
import es.bvalero.replacer.persistence.ReplacementType;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ReplacementRepository replacementRepository;

    @Mock
    private IWikipediaFacade wikipediaFacade;

    @Mock
    private List<IgnoredReplacementFinder> ignoredReplacementFinders;

    @Mock
    private List<ArticleReplacementFinder> articleReplacementFinders;

    @InjectMocks
    private ArticleService articleService;

    @Before
    public void setUp() {
        articleService = new ArticleService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindReplacementsEmpty() {
        ArticleReplacementFinder finder = Mockito.mock(ArticleReplacementFinder.class);
        Mockito.when(articleReplacementFinders.iterator())
                .thenReturn(Collections.singletonList(finder).iterator());

        Assert.assertTrue(articleService.findReplacements("").isEmpty());
    }

    @Test
    public void testFindReplacements() {
        ArticleReplacement replacement = ArticleReplacement.builder()
                .setType(ReplacementType.MISSPELLING)
                .setSubtype("")
                .build();
        ArticleReplacementFinder finder = Mockito.mock(ArticleReplacementFinder.class);
        Mockito.when(finder.findReplacements(Mockito.anyString()))
                .thenReturn(Collections.singletonList(replacement));
        Mockito.when(articleReplacementFinders.iterator())
                .thenReturn(Collections.singletonList(finder).iterator());
        Mockito.when(ignoredReplacementFinders.iterator()).thenReturn(Collections.emptyIterator());

        List<ArticleReplacement> replacements = articleService.findReplacements("");

        Assert.assertFalse(replacements.isEmpty());
        Assert.assertEquals(1, replacements.size());
        Assert.assertTrue(replacements.contains(replacement));
    }

    @Test
    public void testFindReplacementsIgnoringExceptions() {
        ArticleReplacement replacement1 = ArticleReplacement.builder()
                .setStart(0)
                .setText("1")
                .setType(ReplacementType.MISSPELLING)
                .setSubtype("1")
                .build();
        ArticleReplacement replacement2 = ArticleReplacement.builder()
                .setStart(1)
                .setText("2")
                .setType(ReplacementType.MISSPELLING)
                .setSubtype("2")
                .build();
        ArticleReplacementFinder finder = Mockito.mock(ArticleReplacementFinder.class);
        Mockito.when(finder.findReplacements(Mockito.anyString()))
                .thenReturn(Arrays.asList(replacement1, replacement2));
        Mockito.when(articleReplacementFinders.iterator())
                .thenReturn(Collections.singletonList(finder).iterator());

        ArticleReplacement ignored1 = ArticleReplacement.builder()
                .setStart(0)
                .setText("1")
                .setType(ReplacementType.IGNORED)
                .build();
        IgnoredReplacementFinder ignoredFinder = Mockito.mock(IgnoredReplacementFinder.class);
        Mockito.when(ignoredFinder.findIgnoredReplacements(Mockito.anyString()))
                .thenReturn(Collections.singletonList(ignored1));
        Mockito.when(ignoredReplacementFinders.iterator())
                .thenReturn(Collections.singletonList(ignoredFinder).iterator());

        List<ArticleReplacement> replacements = articleService.findReplacements("");

        Assert.assertFalse(replacements.isEmpty());
        Assert.assertEquals(1, replacements.size());
        Assert.assertTrue(replacements.contains(replacement2));
    }

    @Test
    public void testFindRandomArticleWithReplacements()
            throws WikipediaException, InvalidArticleException, UnfoundArticleException {
        String title = "España";
        String text = "Un texto";

        Article randomArticle = Article.builder()
                .setTitle(title)
                .build();
        Mockito.when(articleRepository.findRandomArticleNotReviewed(Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(randomArticle));
        Mockito.when(wikipediaFacade.getArticleContent(Mockito.anyString())).thenReturn(text);

        // Exception matches
        IgnoredReplacementFinder ignoredReplacementFinder = Mockito.mock(IgnoredReplacementFinder.class);
        ArticleReplacement ignored = ArticleReplacement.builder()
                .setStart(10).setText("X").setType(ReplacementType.IGNORED).build();
        Mockito.when(ignoredReplacementFinder.findIgnoredReplacements(Mockito.anyString()))
                .thenReturn(Collections.singletonList(ignored));
        Mockito.when(ignoredReplacementFinders.iterator())
                .thenReturn(Collections.singletonList(ignoredReplacementFinder).iterator());

        // Replacement matches
        ArticleReplacementFinder articleReplacementFinder = Mockito.mock(ArticleReplacementFinder.class);
        ArticleReplacement replacement = ArticleReplacement.builder()
                .setStart(0).setText("Z").setType(ReplacementType.MISSPELLING).build();
        Mockito.when(articleReplacementFinder.findReplacements(Mockito.anyString()))
                .thenReturn(Collections.singletonList(replacement));
        Mockito.when(articleReplacementFinders.iterator())
                .thenReturn(Collections.singletonList(articleReplacementFinder).iterator());

        ArticleReview articleData = articleService.findRandomArticleWithReplacements();

        Assert.assertNotNull(articleData);
        Assert.assertEquals(title, articleData.getTitle());
        Assert.assertEquals(text, articleData.getContent());

        List<ArticleReplacement> replacements = articleData.getReplacements();
        Assert.assertEquals(1, replacements.size());
        Assert.assertTrue(replacements.contains(replacement));
        Assert.assertFalse(replacements.contains(ignored));
    }

    @Test
    public void testSaveArticle() throws WikipediaException {
        String title = "España";
        String text = "Un texto";

        Article articleDb = Article.builder().build();
        Mockito.when(articleRepository.findByTitle(Mockito.anyString())).thenReturn(articleDb);

        articleService.saveArticleChanges(title, text);

        Mockito.verify(wikipediaFacade).editArticleContent(title, text);
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
