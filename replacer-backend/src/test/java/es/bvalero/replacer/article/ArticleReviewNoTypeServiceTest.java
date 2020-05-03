package es.bvalero.replacer.article;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.replacement.ReplacementRepository;
import es.bvalero.replacer.wikipedia.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;

public class ArticleReviewNoTypeServiceTest {
    private final int randomId = 1;
    private final int randomId2 = 2;
    private final String content = "XYZ";
    private final String content2 = "Y";
    private final WikipediaPage article = WikipediaPage
        .builder()
        .id(randomId)
        .lang(WikipediaLanguage.SPANISH)
        .namespace(WikipediaNamespace.ARTICLE)
        .content(content)
        .lastUpdate(LocalDate.now())
        .build();
    private final WikipediaPage article2 = WikipediaPage
        .builder()
        .id(randomId2)
        .lang(WikipediaLanguage.SPANISH)
        .namespace(WikipediaNamespace.ANNEX)
        .content(content2)
        .lastUpdate(LocalDate.now())
        .build();
    private final int offset = 1;
    private final Replacement replacement = Replacement
        .builder()
        .start(offset)
        .type("X")
        .subtype("Y")
        .text("Y")
        .build();
    private final List<Replacement> replacements = Collections.singletonList(replacement);
    private final ArticleReviewOptions options = ArticleReviewOptions.ofNoType();

    @Mock
    private ReplacementRepository replacementRepository;

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private ReplacementIndexService replacementIndexService;

    @Mock
    private ReplacementFindService replacementFindService;

    @Mock
    private SectionReviewService sectionReviewService;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private ArticleReviewNoTypeService articleService;

    @BeforeEach
    public void setUp() {
        articleService = new ArticleReviewNoTypeService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindRandomArticleToReviewNoTypeNoResultInDb() {
        // No results in DB
        Mockito
            .when(replacementRepository.findRandomArticleIdsToReview(Mockito.any(PageRequest.class)))
            .thenReturn(Collections.emptyList());

        Optional<ArticleReview> review = articleService.findRandomArticleReview(options);

        Assertions.assertFalse(review.isPresent());
    }

    @Test
    public void testFindRandomArticleToReviewNoTypeNotInWikipedia() throws ReplacerException {
        // 1 result in DB
        Mockito
            .when(replacementRepository.findRandomArticleIdsToReview(Mockito.any(PageRequest.class)))
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)))
            .thenReturn(Collections.emptyList());

        // The article doesn't exist in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId, WikipediaLanguage.SPANISH)).thenReturn(Optional.empty());

        Optional<ArticleReview> review = articleService.findRandomArticleReview(options);

        Assertions.assertFalse(review.isPresent());
    }

    @Test
    public void testFindRandomArticleToReviewNoTypeWithReplacements() throws ReplacerException {
        // 1 result in DB
        Mockito
            .when(replacementRepository.findRandomArticleIdsToReview(Mockito.any(PageRequest.class)))
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)));

        // The article exists in Wikipedia
        Mockito
            .when(wikipediaService.getPageById(randomId, WikipediaLanguage.SPANISH))
            .thenReturn(Optional.of(article));

        // The article contains replacements
        Mockito
            .when(replacementFindService.findReplacements(content, WikipediaLanguage.SPANISH))
            .thenReturn(replacements);

        Optional<ArticleReview> review = articleService.findRandomArticleReview(options);

        Mockito
            .verify(replacementIndexService, Mockito.times(1))
            .indexArticleReplacements(Mockito.eq(randomId), Mockito.any(WikipediaLanguage.class), Mockito.anyList());

        Assertions.assertTrue(review.isPresent());
        Assertions.assertEquals(randomId, review.get().getId());
    }

    @Test
    public void testFindRandomArticleToReviewNoTypeNoReplacements() throws ReplacerException {
        // 1 result in DB
        Mockito
            .when(replacementRepository.findRandomArticleIdsToReview(Mockito.any(PageRequest.class)))
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)))
            .thenReturn(Collections.emptyList());

        // The article exists in Wikipedia
        Mockito
            .when(wikipediaService.getPageById(randomId, WikipediaLanguage.SPANISH))
            .thenReturn(Optional.of(article));

        // The article doesn't contain replacements
        List<Replacement> noArticleReplacements = Collections.emptyList();
        Mockito
            .when(replacementFindService.findReplacements(content, WikipediaLanguage.SPANISH))
            .thenReturn(noArticleReplacements);

        Optional<ArticleReview> review = articleService.findRandomArticleReview(options);

        Mockito
            .verify(replacementIndexService, Mockito.times(1))
            .indexArticleReplacements(randomId, WikipediaLanguage.SPANISH, Collections.emptyList());

        Assertions.assertFalse(review.isPresent());
    }

    @Test
    public void testFindRandomArticleToReviewNoTypeSecondResult() throws ReplacerException {
        // 2 results in DB
        Mockito
            .when(replacementRepository.findRandomArticleIdsToReview(Mockito.any(PageRequest.class)))
            .thenReturn(new ArrayList<>(Arrays.asList(randomId, randomId2)));

        // Only the article 2 exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId, WikipediaLanguage.SPANISH)).thenReturn(Optional.empty());
        Mockito
            .when(wikipediaService.getPageById(randomId2, WikipediaLanguage.SPANISH))
            .thenReturn(Optional.of(article2));

        // The article contains replacements
        Mockito
            .when(replacementFindService.findReplacements(content2, WikipediaLanguage.SPANISH))
            .thenReturn(replacements);

        Optional<ArticleReview> review = articleService.findRandomArticleReview(options);

        Mockito
            .verify(replacementIndexService, Mockito.times(1))
            .indexArticleReplacements(Mockito.eq(randomId2), Mockito.any(WikipediaLanguage.class), Mockito.anyList());

        Assertions.assertTrue(review.isPresent());
        Assertions.assertEquals(randomId2, review.get().getId());
    }
}
