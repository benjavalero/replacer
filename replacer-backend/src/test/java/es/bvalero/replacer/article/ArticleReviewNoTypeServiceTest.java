package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;

import java.util.*;

public class ArticleReviewNoTypeServiceTest {

    private final int randomId = 1;
    private final int randomId2 = 2;
    private final String content = "XYZ";
    private final String content2 = "Y";
    private final WikipediaPage article = WikipediaPage.builder()
            .id(randomId).namespace(WikipediaNamespace.ARTICLE).content(content)
            .build();
    private final WikipediaPage article2 = WikipediaPage.builder()
            .id(randomId2).namespace(WikipediaNamespace.ANNEX).content(content2)
            .build();
    private final int offset = 1;
    private final Replacement replacement =
            Replacement.builder().start(offset).type("X").subtype("Y").text("Y").build();
    private final List<Replacement> replacements = Collections.singletonList(replacement);

    @Mock
    private ReplacementRepository replacementRepository;

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private ArticleIndexService articleIndexService;

    @Mock
    private ReplacementFinderService replacementFinderService;

    @Mock
    private SectionReviewService sectionReviewService;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private ArticleReviewNoTypeService articleService;

    @Before
    public void setUp() {
        articleService = new ArticleReviewNoTypeService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindRandomArticleToReviewNoTypeNoResultInDb() {
        // No results in DB
        Mockito.when(replacementRepository.findRandomArticleIdsToReview(Mockito.any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        Optional<ArticleReview> review = articleService.findRandomArticleReview();

        Assert.assertFalse(review.isPresent());
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

        Optional<ArticleReview> review = articleService.findRandomArticleReview();

        Assert.assertFalse(review.isPresent());
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
                .thenReturn(replacements);

        Optional<ArticleReview> review = articleService.findRandomArticleReview();

        Mockito.verify(articleIndexService, Mockito.times(1))
                .indexArticleReplacements(article, replacements);

        Assert.assertTrue(review.isPresent());
        Assert.assertEquals(randomId, review.get().getId());
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
        List<Replacement> noArticleReplacements = Collections.emptyList();
        Mockito.when(replacementFinderService.findReplacements(content))
                .thenReturn(noArticleReplacements);

        Optional<ArticleReview> review = articleService.findRandomArticleReview();

        Mockito.verify(articleIndexService, Mockito.times(1))
                .indexArticleReplacements(article, noArticleReplacements);

        Assert.assertFalse(review.isPresent());
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
                .thenReturn(Optional.of(article2));

        // The article contains replacements
        Mockito.when(replacementFinderService.findReplacements(content2))
                .thenReturn(replacements);

        Optional<ArticleReview> review = articleService.findRandomArticleReview();

        Mockito.verify(articleIndexService, Mockito.times(1))
                .indexArticleReplacements(article2, replacements);

        Assert.assertTrue(review.isPresent());
        Assert.assertEquals(randomId2, review.get().getId());
    }

}
