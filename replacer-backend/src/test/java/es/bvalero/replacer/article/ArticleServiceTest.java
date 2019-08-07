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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ArticleServiceTest {

    private final int randomId = 1;
    private final String content = "X";
    private final WikipediaPage article = WikipediaPage.builder().content(content).build();
    private final ArticleReplacement articleReplacement =
            new ArticleReplacement("", 0, "", "", Collections.emptyList());
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
                .thenReturn(new ArrayList<>(Collections.singleton(randomId)));

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
                .thenReturn(new ArrayList<>(Collections.singleton(randomId)));

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

}
