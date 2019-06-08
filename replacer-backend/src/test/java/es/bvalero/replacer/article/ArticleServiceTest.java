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
import org.springframework.data.domain.PageRequest;

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


        articleService.indexArticleReplacements(newReplacements, dbReplacements1, true);
        articleService.flushReplacements();


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

        Replacement rep2 = new Replacement(1, "", "", 2)
                .withStatus(ReplacementStatus.TO_REVIEW); // Obsolete To review => DELETE
        Replacement rep3 = new Replacement(1, "", "", 3)
                .withStatus(ReplacementStatus.REVIEWED); // Obsolete Reviewed => DO NOTHING
        Replacement rep4 = new Replacement(1, "", "", 4)
                .withStatus(ReplacementStatus.FIXED); // Obsolete Fixed => DO NOTHING
        List<Replacement> dbReplacements1 = new ArrayList<>(Arrays.asList(rep2, rep3, rep4));


        articleService.indexArticleReplacements(newReplacements, dbReplacements1, true);
        articleService.flushReplacements();


        Mockito.verify(replacementRepository, Mockito.times(1)).deleteInBatch(
                Collections.singleton(rep2)
        );
        Mockito.verify(replacementRepository, Mockito.times(1)).saveAll(
                Collections.emptySet()
        );
    }

    @Test
    public void testIndexExistingArticle() {
        Replacement rep7 = new Replacement(2, "", "", 1);
        Replacement rep9 = new Replacement(2, "", "", 2);
        Replacement rep11 = new Replacement(2, "", "", 3);
        Replacement rep13 = new Replacement(2, "", "", 4);
        Replacement rep15 = new Replacement(2, "", "", 5);
        List<Replacement> newReplacements = Arrays.asList(rep7, rep9, rep11, rep13, rep15);

        Replacement rep8 = rep7.withStatus(ReplacementStatus.TO_REVIEW)
                .withLastUpdate(rep7.getLastUpdate().minusDays(1)); // Existing To Review Older => UPDATE
        Replacement rep10 = rep9.withStatus(ReplacementStatus.TO_REVIEW); // Existing To Review Equal => DO NOTHING
        Replacement rep12 = rep11.withStatus(ReplacementStatus.REVIEWED); // Existing Reviewed => DO NOTHING
        Replacement rep14 = rep13.withStatus(ReplacementStatus.FIXED)
                .withLastUpdate(rep13.getLastUpdate().plusDays(1)); // Existing Fixed Newer => DO NOTHING
        Replacement rep16 = rep15.withStatus(ReplacementStatus.FIXED)
                .withLastUpdate(rep15.getLastUpdate().minusDays(1)); // Existing Fixed Older => UPDATE STATUS
        List<Replacement> dbReplacements2 = new ArrayList<>(Arrays.asList(rep8, rep10, rep12, rep14, rep16));


        articleService.indexArticleReplacements(newReplacements, dbReplacements2, true);
        articleService.flushReplacements();


        Mockito.verify(replacementRepository, Mockito.times(1)).deleteInBatch(
                Collections.emptySet()
        );
        Mockito.verify(replacementRepository, Mockito.times(1)).saveAll(
                new HashSet<>(Arrays.asList(
                        rep8.withLastUpdate(rep7.getLastUpdate()),
                        rep16.withLastUpdate(rep15.getLastUpdate()).withStatus(ReplacementStatus.TO_REVIEW)))
        );
    }

    @Test
    public void testFindRandomArticleWithReplacements() throws WikipediaException, UnfoundArticleException {
        String title = "España";
        String text = "Un texto";

        Replacement randomReplacement = new Replacement(1, "", "", 1);
        Mockito.when(replacementRepository.findRandomByStatus(
                Mockito.any(ReplacementStatus.class), Mockito.any(PageRequest.class)))
                .thenReturn(Collections.singletonList(randomReplacement));

        WikipediaPage page = WikipediaPage.builder().setTitle(title).setContent(text).build();
        Mockito.when(wikipediaService.getPageById(Mockito.anyInt())).thenReturn(Optional.of(page));

        // Replacement matches
        ArticleReplacement replacement = Mockito.mock(ArticleReplacement.class);
        Mockito.when(replacementFinderService.findReplacements(text)).thenReturn(Collections.singletonList(replacement));

        ArticleReview articleData = articleService.findRandomArticleToReview();

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

        WikipediaPage page = WikipediaPage.builder().build();
        Mockito.when(wikipediaService.getPageByTitle(title)).thenReturn(Optional.of(page));

        Replacement replacement = new Replacement(1, "", "", 1);
        Mockito.when(replacementRepository.findByArticleIdAndStatus(Mockito.anyInt(), Mockito.any(ReplacementStatus.class)))
                .thenReturn(Collections.singletonList(replacement));

        OAuth1AccessToken accessToken = Mockito.mock(OAuth1AccessToken.class);

        articleService.saveArticleChanges(title, text, accessToken);

        Mockito.verify(wikipediaService).savePageContent(
                Mockito.eq(title), Mockito.eq(text), Mockito.any(LocalDateTime.class), Mockito.eq(accessToken));
        Mockito.verify(replacementRepository).save(Mockito.any(Replacement.class));
    }

}
