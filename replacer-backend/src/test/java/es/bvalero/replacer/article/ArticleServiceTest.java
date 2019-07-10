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
import java.util.*;

public class ArticleServiceTest {

    @Mock
    private ReplacementFinderService replacementFinderService;

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
    public void testIndexNewArticle() {
        Replacement rep1 = new Replacement(1, "", "", 1); // New => ADD
        List<Replacement> newReplacements = Collections.singletonList(rep1);

        List<Replacement> dbReplacements1 = Collections.emptyList();


        WikipediaPage article = WikipediaPage.builder().build();
        articleService.indexReplacements(article, newReplacements, dbReplacements1, true);
        articleService.flushReplacementsInBatch();


        Mockito.verify(replacementRepository, Mockito.times(1)).deleteInBatch(
                Collections.emptySet()
        );
        Mockito.verify(replacementRepository, Mockito.times(1)).saveAll(
                Collections.singleton(rep1)
        );
    }

    @Test
    public void testIndexObsoleteArticle() {
        List<Replacement> newReplacements = Collections.emptyList();

        // REVIEWED <=> exists reviewer
        Replacement rep2 = new Replacement(1, "", "", 2); // Obsolete To review => DELETE
        Replacement rep3 = new Replacement(1, "", "", 3)
                .withReviewer("x"); // Obsolete Reviewed => DO NOTHING
        List<Replacement> dbReplacements1 = new ArrayList<>(Arrays.asList(rep2, rep3));


        WikipediaPage article = WikipediaPage.builder().setTimestamp(LocalDateTime.now()).build();
        articleService.indexReplacements(article, newReplacements, dbReplacements1, true);
        articleService.flushReplacementsInBatch();


        Mockito.verify(replacementRepository, Mockito.times(1)).deleteInBatch(
                Collections.singleton(rep2)
        );
        Mockito.verify(replacementRepository, Mockito.times(1)).saveAll(
                Collections.emptySet()
        );
    }

    @Test
    public void testIndexArticleWithoutReplacements() {
        List<Replacement> newReplacements = Collections.emptyList();
        List<Replacement> dbReplacements = Collections.emptyList();

        WikipediaPage article = WikipediaPage.builder().setTimestamp(LocalDateTime.now()).build();
        articleService.indexReplacements(article, newReplacements, dbReplacements, true);
        articleService.flushReplacementsInBatch();

        // Save the fake replacement
        Mockito.verify(replacementRepository, Mockito.times(1)).deleteInBatch(
                Collections.emptySet()
        );
        Mockito.verify(replacementRepository, Mockito.times(1)).saveAll(Mockito.anyIterable());
    }

    @Test
    public void testIndexExistingArticle() {
        Replacement rep7 = new Replacement(2, "", "", 1);
        Replacement rep9 = new Replacement(2, "", "", 2);

        Replacement rep11 = new Replacement(2, "", "", 3);
        Replacement rep17 = new Replacement(2, "", "", 6);
        Replacement rep19 = new Replacement(2, "", "", 7);

        List<Replacement> newReplacements = Arrays.asList(rep7, rep9, rep11, rep17, rep19);

        // If the existing replacement is not reviewed and:
        // - Is older => Update the "lastUpdate" to help skipping future indexations
        // - Is equal => All as in the last indexation => nothing to do
        // - Is newer => Impossible case

        // If the existing replacement is reviewed/fixed and:
        // - Is older => Update the "lastUpdate" to help skipping future indexations
        // - Is equal => All as in the last indexation => nothing to do
        // - Is newer => Reviewed/fixed after indexation => nothing to do

        // REVIEWED <=> exists reviewer
        Replacement rep8 = rep7.withLastUpdate(rep7.getLastUpdate().minusDays(1)); // Existing To Review Older => UPDATE DATE
        Replacement rep10 = rep9.withLastUpdate(rep9.getLastUpdate()); // Existing To Review Equal => DO NOTHING

        Replacement rep12 = rep11.withReviewer("x")
                .withLastUpdate(rep11.getLastUpdate().minusDays(1)); // Existing Reviewed Older => UPDATE DATE
        Replacement rep18 = rep17.withReviewer("x"); // Existing Reviewed Equal => DO NOTHING
        Replacement rep20 = rep19.withReviewer("x")
                .withLastUpdate(rep19.getLastUpdate().plusDays(1)); // Existing Reviewed Newer => DO NOTHING

        List<Replacement> dbReplacements2 = new ArrayList<>(Arrays.asList(rep8, rep10, rep12, rep18, rep20));

        WikipediaPage article = WikipediaPage.builder().build();
        articleService.indexReplacements(article, newReplacements, dbReplacements2, true);
        articleService.flushReplacementsInBatch();


        Mockito.verify(replacementRepository, Mockito.times(1)).deleteInBatch(
                Collections.emptySet()
        );
        Mockito.verify(replacementRepository, Mockito.times(1)).saveAll(
                new HashSet<>(Arrays.asList(
                        rep8.withLastUpdate(rep7.getLastUpdate()),
                        rep12.withLastUpdate(rep11.getLastUpdate())))
        );
    }

    @Test
    public void testFindArticleReview() throws WikipediaException {
        String title = "España";
        String text = "Un texto";

        WikipediaPage page = WikipediaPage.builder().setTitle(title).setContent(text).setTimestamp(LocalDateTime.now()).build();
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
        Mockito.verify(replacementRepository).save(Mockito.any(Replacement.class));
    }

}
