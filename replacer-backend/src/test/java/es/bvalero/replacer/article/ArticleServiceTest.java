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
import org.springframework.data.domain.PageRequest;

import java.util.*;

public class ArticleServiceTest {

    private final int randomId = 1;
    private final int randomId2 = 2;
    private final String content = "X";
    private final String content2 = "Y";
    private final WikipediaPage article = WikipediaPage.builder().content(content).build();
    private final WikipediaPage article2 = WikipediaPage.builder().content(content2).build();
    private final ArticleReplacement articleReplacement =
            ArticleReplacement.builder().type("X").subtype("Y").build();
    private final List<ArticleReplacement> articleReplacements = Collections.singletonList(articleReplacement);

    @Mock
    private ReplacementRepository replacementRepository;

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private ArticleStatsService articleStatsService;

    @Mock
    private ArticleIndexService articleIndexService;

    @Mock
    private ReplacementFinderService replacementFinderService;

    @InjectMocks
    private ArticleService articleService;

    @Before
    public void setUp() {
        articleService = new ArticleService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindRandomArticleToReviewNoTypeNoResultInDb() {
        // No results in DB
        Mockito.when(replacementRepository.findRandomArticleIdsToReview(Mockito.any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        Optional<Integer> articleId = articleService.findRandomArticleToReview();

        Assert.assertFalse(articleId.isPresent());
    }

    @Test
    public void testFindRandomArticleToReviewNoTypeNotInWikipedia() throws WikipediaException {
        // 1 result in DB
        Mockito.when(replacementRepository.findRandomArticleIdsToReview(Mockito.any(PageRequest.class)))
                .thenReturn(new ArrayList<>(Collections.singleton(randomId)))
                .thenReturn(Collections.emptyList());

        // The article doesn't exist in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId))
                .thenReturn(Optional.empty());

        Optional<Integer> articleId = articleService.findRandomArticleToReview();

        Assert.assertFalse(articleId.isPresent());
    }

    @Test
    public void testFindRandomArticleToReviewNoTypeWithReplacements() throws WikipediaException {
        // 1 result in DB
        Mockito.when(replacementRepository.findRandomArticleIdsToReview(Mockito.any(PageRequest.class)))
                .thenReturn(new ArrayList<>(Collections.singleton(randomId)));

        // The article exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId))
                .thenReturn(Optional.of(article));

        // The article contains replacements
        Mockito.when(replacementFinderService.findReplacements(content))
                .thenReturn(articleReplacements);

        Optional<Integer> articleId = articleService.findRandomArticleToReview();

        Mockito.verify(articleIndexService, Mockito.times(1))
                .indexArticleReplacements(article, articleReplacements);

        Assert.assertTrue(articleId.isPresent());
        Assert.assertEquals(Optional.of(randomId), articleId);
    }

    @Test
    public void testFindRandomArticleToReviewNoTypeNoReplacements() throws WikipediaException {
        // 1 result in DB
        Mockito.when(replacementRepository.findRandomArticleIdsToReview(Mockito.any(PageRequest.class)))
                .thenReturn(new ArrayList<>(Collections.singleton(randomId)))
                .thenReturn(Collections.emptyList());

        // The article exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId))
                .thenReturn(Optional.of(article));

        // The article doesn't contain replacements
        List<ArticleReplacement> noArticleReplacements = Collections.emptyList();
        Mockito.when(replacementFinderService.findReplacements(content))
                .thenReturn(noArticleReplacements);

        Optional<Integer> articleId = articleService.findRandomArticleToReview();

        Mockito.verify(articleIndexService, Mockito.times(1))
                .indexArticleReplacements(article, noArticleReplacements);

        Assert.assertFalse(articleId.isPresent());
    }

    @Test
    public void testFindRandomArticleToReviewNoTypeSecondResult() throws WikipediaException {
        // 2 results in DB
        Mockito.when(replacementRepository.findRandomArticleIdsToReview(Mockito.any(PageRequest.class)))
                .thenReturn(new ArrayList<>(Arrays.asList(randomId, randomId2)));

        // Only the article 2 exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId))
                .thenReturn(Optional.empty());
        Mockito.when(wikipediaService.getPageById(randomId2))
                .thenReturn(Optional.of(article));

        // The article contains replacements
        Mockito.when(replacementFinderService.findReplacements(content))
                .thenReturn(articleReplacements);

        Optional<Integer> articleId = articleService.findRandomArticleToReview();

        Mockito.verify(articleIndexService, Mockito.times(1))
                .indexArticleReplacements(article, articleReplacements);

        Assert.assertTrue(articleId.isPresent());
        Assert.assertEquals(Optional.of(randomId2), articleId);
    }

    @Test
    public void testFindRandomArticleToReviewTypeNotFiltered() throws WikipediaException {
        // 1 result in DB
        Mockito.when(replacementRepository.findRandomArticleIdsToReviewByTypeAndSubtype(
                Mockito.anyString(), Mockito.anyString(), Mockito.any(PageRequest.class)))
                .thenReturn(new ArrayList<>(Collections.singleton(randomId)))
                .thenReturn(Collections.emptyList());

        // The article exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId))
                .thenReturn(Optional.of(article));

        // The article contains replacements
        Mockito.when(replacementFinderService.findReplacements(content))
                .thenReturn(articleReplacements);

        Optional<Integer> articleId = articleService.findRandomArticleToReview("A", "B");

        Mockito.verify(articleIndexService, Mockito.times(1))
                .indexArticleReplacements(article, articleReplacements);

        Assert.assertFalse(articleId.isPresent());
    }

    @Test
    public void testFindRandomArticleToReviewTypeFiltered() throws WikipediaException {
        // 1 result in DB
        Mockito.when(replacementRepository.findRandomArticleIdsToReviewByTypeAndSubtype(
                Mockito.anyString(), Mockito.anyString(), Mockito.any(PageRequest.class)))
                .thenReturn(new ArrayList<>(Collections.singleton(randomId)));

        // The article exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId))
                .thenReturn(Optional.of(article));

        // The article contains replacements
        Mockito.when(replacementFinderService.findReplacements(content))
                .thenReturn(articleReplacements);

        Optional<Integer> articleId = articleService.findRandomArticleToReview("X", "Y");

        Mockito.verify(articleIndexService, Mockito.times(1))
                .indexArticleReplacements(article, articleReplacements);

        Assert.assertTrue(articleId.isPresent());
        Assert.assertEquals(Optional.of(randomId), articleId);
    }

    @Test
    public void testFindRandomArticleToReviewCustom() throws WikipediaException {
        final String replacement = "R";
        final String suggestion = "S";

        // 1 result in Wikipedia
        Mockito.when(wikipediaService.getPageIdsByStringMatch(Mockito.anyString()))
                .thenReturn(Collections.singleton(randomId));

        // The article exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId))
                .thenReturn(Optional.of(article));

        // The result is not already reviewed
        Mockito.when(replacementRepository.countByArticleIdAndTypeAndSubtypeAndReviewerNotNull(
                randomId, ReplacementFinderService.CUSTOM_FINDER_TYPE, replacement))
                .thenReturn(0L);

        // The article contains replacements
        Mockito.when(replacementFinderService.findCustomReplacements(content, replacement, suggestion))
                .thenReturn(articleReplacements);

        Optional<Integer> articleId =
                articleService.findRandomArticleToReviewWithCustomReplacement(replacement, suggestion);

        Assert.assertTrue(articleId.isPresent());
        Assert.assertEquals(Optional.of(randomId), articleId);
    }

    @Test
    public void testFindRandomArticleToReviewCustomNoResults() throws WikipediaException {
        final String replacement = "R";
        final String suggestion = "S";

        // 2 results in Wikipedia
        Mockito.when(wikipediaService.getPageIdsByStringMatch(Mockito.anyString()))
                .thenReturn(new HashSet<>(Arrays.asList(randomId, randomId2)));

        // The result 1 is already reviewed
        // The result 2 is not reviewed the first time, but reviewed the second time.
        Mockito.when(replacementRepository.countByArticleIdAndTypeAndSubtypeAndReviewerNotNull(
                randomId, ReplacementFinderService.CUSTOM_FINDER_TYPE, replacement))
                .thenReturn(1L);
        Mockito.when(replacementRepository.countByArticleIdAndTypeAndSubtypeAndReviewerNotNull(
                randomId2, ReplacementFinderService.CUSTOM_FINDER_TYPE, replacement))
                .thenReturn(0L).thenReturn(1L);

        // The articles exist in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId))
                .thenReturn(Optional.of(article));
        Mockito.when(wikipediaService.getPageById(randomId2))
                .thenReturn(Optional.of(article2));

        // The article 2 contains no replacements
        Mockito.when(replacementFinderService.findCustomReplacements(content2, replacement, suggestion))
                .thenReturn(Collections.emptyList());

        Optional<Integer> articleId =
                articleService.findRandomArticleToReviewWithCustomReplacement(replacement, suggestion);

        Mockito.verify(articleIndexService, Mockito.times(1))
                .reviewReplacementAsSystem(Mockito.any(Replacement.class), Mockito.eq(false));

        Assert.assertFalse(articleId.isPresent());
    }

    @Test
    public void testFindRandomArticleToReviewNoTypeAndThenFiltered() throws WikipediaException {
        // 1. Find the random article 1 by type. In DB there exists also the article 2.
        // 2. Find the random article 2 by no type. The article 2 is supposed to be removed from all the caches.
        // 3. Find a random article by type. In DB there is no article.

        // 2 results in DB by type, no results the second time.
        Mockito.when(replacementRepository.findRandomArticleIdsToReviewByTypeAndSubtype(
                Mockito.anyString(), Mockito.anyString(), Mockito.any(PageRequest.class)))
                .thenReturn(new ArrayList<>(Arrays.asList(randomId, randomId2)))
                .thenReturn(Collections.emptyList());
        // 1 result in DB by no type
        Mockito.when(replacementRepository.findRandomArticleIdsToReview(Mockito.any(PageRequest.class)))
                .thenReturn(new ArrayList<>(Collections.singletonList(randomId2)));

        // The articles exist in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId))
                .thenReturn(Optional.of(article));
        Mockito.when(wikipediaService.getPageById(randomId2))
                .thenReturn(Optional.of(article2));

        // The articles contains replacements
        Mockito.when(replacementFinderService.findReplacements(content))
                .thenReturn(articleReplacements);
        Mockito.when(replacementFinderService.findReplacements(content2))
                .thenReturn(articleReplacements);

        Optional<Integer> articleId = articleService.findRandomArticleToReview("X", "Y");
        Assert.assertEquals(Optional.of(randomId), articleId);

        articleId = articleService.findRandomArticleToReview();
        Assert.assertEquals(Optional.of(randomId2), articleId);

        articleId = articleService.findRandomArticleToReview("X", "Y");
        Assert.assertFalse(articleId.isPresent());
    }

}
