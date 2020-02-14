package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementCountService;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.replacement.ReplacementRepository;
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

import java.time.LocalDate;
import java.util.*;

public class ArticleReviewTypeSubtypeServiceTest {

    private final int randomId = 1;
    private final int randomId2 = 2;
    private final String content = "XYZ";
    private final String content2 = "Y";
    private final WikipediaPage article = WikipediaPage.builder()
            .id(randomId).namespace(WikipediaNamespace.ARTICLE).content(content).lastUpdate(LocalDate.now())
            .build();
    private final WikipediaPage article2 = WikipediaPage.builder()
            .id(randomId2).namespace(WikipediaNamespace.ANNEX).content(content2).lastUpdate(LocalDate.now())
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
    private ReplacementFindService replacementFindService;

    @Mock
    private ReplacementIndexService replacementIndexService;

    @Mock
    private SectionReviewService sectionReviewService;

    @Mock
    private ReplacementCountService articleStatsService;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private ArticleReviewTypeSubtypeService articleService;

    @Before
    public void setUp() {
        articleService = new ArticleReviewTypeSubtypeService();
        MockitoAnnotations.initMocks(this);
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
        Mockito.when(replacementFindService.findReplacements(content))
                .thenReturn(replacements);

        Optional<ArticleReview> review = articleService.findRandomArticleReview("A", "B");

        Mockito.verify(replacementIndexService, Mockito.times(1))
                .indexArticleReplacements(Mockito.eq(randomId), Mockito.anyList());

        Assert.assertFalse(review.isPresent());
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
        Mockito.when(replacementFindService.findReplacements(content))
                .thenReturn(replacements);

        Optional<ArticleReview> review = articleService.findRandomArticleReview("X", "Y");

        Mockito.verify(replacementIndexService, Mockito.times(1))
                .indexArticleReplacements(Mockito.eq(randomId), Mockito.anyList());

        Assert.assertTrue(review.isPresent());
        Assert.assertEquals(randomId, review.get().getId());
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
        Mockito.when(replacementFindService.findReplacements(content))
                .thenReturn(replacements);
        Mockito.when(replacementFindService.findReplacements(content2))
                .thenReturn(replacements);

        Optional<ArticleReview> review = articleService.findRandomArticleReview("X", "Y");
        Assert.assertTrue(review.isPresent());
        Assert.assertEquals(randomId, review.get().getId());

        review = articleService.findRandomArticleReview();
        Assert.assertTrue(review.isPresent());
        Assert.assertEquals(randomId2, review.get().getId());

        review = articleService.findRandomArticleReview("X", "Y");
        Assert.assertFalse(review.isPresent());
    }

    @Test
    public void testArticleReviewWithSection() throws WikipediaException {
        final int sectionId = 1;

        // The article exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId))
                .thenReturn(Optional.of(article));

        // The article contains replacements
        Mockito.when(replacementFindService.findReplacements(content))
                .thenReturn(replacements);

        // The article has sections
        ArticleReview sectionReview = articleService.buildArticleReview(article, replacements);
        sectionReview.setSection(sectionId);
        Mockito.when(sectionReviewService.findSectionReview(Mockito.any(ArticleReview.class))).thenReturn(Optional.of(sectionReview));

        Optional<ArticleReview> review = articleService.getArticleReview(randomId, "X", "Y");

        Assert.assertTrue(review.isPresent());
        review.ifPresent(rev -> {
            Assert.assertEquals(randomId, rev.getId());
            Assert.assertEquals(replacements.size(), rev.getReplacements().size());
            Assert.assertEquals(sectionId, rev.getSection().intValue());
        });
    }

    @Test
    public void testArticleReviewWithNoSection() throws WikipediaException {
        // The article exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId))
                .thenReturn(Optional.of(article));

        // The article contains replacements
        Mockito.when(replacementFindService.findReplacements(content))
                .thenReturn(replacements);

        // The article has no sections
        Mockito.when(sectionReviewService.findSectionReview(Mockito.any(ArticleReview.class))).thenReturn(Optional.empty());

        Optional<ArticleReview> review = articleService.getArticleReview(randomId, "X", "Y");

        Assert.assertTrue(review.isPresent());
        review.ifPresent(rev -> {
            Assert.assertEquals(randomId, rev.getId());
            Assert.assertEquals(replacements.size(), rev.getReplacements().size());
            Assert.assertNull(rev.getSection());
        });
    }

}