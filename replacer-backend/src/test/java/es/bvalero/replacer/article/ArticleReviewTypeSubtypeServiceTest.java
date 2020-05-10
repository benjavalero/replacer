package es.bvalero.replacer.article;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementCountService;
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

public class ArticleReviewTypeSubtypeServiceTest {
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
    private final ArticleReviewOptions options = ArticleReviewOptions.ofTypeSubtype(
        WikipediaLanguage.SPANISH,
        "X",
        "Y"
    );
    private final ArticleReviewOptions options2 = ArticleReviewOptions.ofTypeSubtype(
        WikipediaLanguage.SPANISH,
        "A",
        "B"
    );

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

    @BeforeEach
    public void setUp() {
        articleService = new ArticleReviewTypeSubtypeService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindRandomArticleToReviewTypeNotFiltered() throws ReplacerException {
        // 1 result in DB
        Mockito
            .when(
                replacementRepository.findRandomArticleIdsToReviewByTypeAndSubtype(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.any(PageRequest.class)
                )
            )
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)))
            .thenReturn(Collections.emptyList());

        // The article exists in Wikipedia
        Mockito
            .when(wikipediaService.getPageById(randomId, WikipediaLanguage.SPANISH))
            .thenReturn(Optional.of(article));

        // The article contains replacements
        Mockito
            .when(replacementFindService.findReplacements(content, WikipediaLanguage.SPANISH))
            .thenReturn(replacements);

        Optional<ArticleReview> review = articleService.findRandomArticleReview(options2);

        Mockito
            .verify(replacementIndexService, Mockito.times(1))
            .indexArticleReplacements(Mockito.eq(randomId), Mockito.any(WikipediaLanguage.class), Mockito.anyList());

        Assertions.assertFalse(review.isPresent());
    }

    @Test
    public void testFindRandomArticleToReviewTypeFiltered() throws ReplacerException {
        // 1 result in DB
        Mockito
            .when(
                replacementRepository.findRandomArticleIdsToReviewByTypeAndSubtype(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.any(PageRequest.class)
                )
            )
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
    public void testFindRandomArticleToReviewNoTypeAndThenFiltered() throws ReplacerException {
        // 1. Find the random article 1 by type. In DB there exists also the article 2.
        // 2. Find the random article 2 by no type. The article 2 is supposed to be removed from all the caches.
        // 3. Find a random article by type. In DB there is no article.

        // 2 results in DB by type, no results the second time.
        Mockito
            .when(
                replacementRepository.findRandomArticleIdsToReviewByTypeAndSubtype(
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.any(PageRequest.class)
                )
            )
            .thenReturn(new ArrayList<>(Arrays.asList(randomId, randomId2)))
            .thenReturn(Collections.emptyList());
        // 1 result in DB by no type
        Mockito
            .when(
                replacementRepository.findRandomArticleIdsToReview(
                    Mockito.anyString(),
                    Mockito.anyLong(),
                    Mockito.any(PageRequest.class)
                )
            )
            .thenReturn(new ArrayList<>(Collections.singletonList(randomId2)));

        // The articles exist in Wikipedia
        Mockito
            .when(wikipediaService.getPageById(randomId, WikipediaLanguage.SPANISH))
            .thenReturn(Optional.of(article));
        Mockito
            .when(wikipediaService.getPageById(randomId2, WikipediaLanguage.SPANISH))
            .thenReturn(Optional.of(article2));

        // The articles contains replacements
        Mockito
            .when(replacementFindService.findReplacements(content, WikipediaLanguage.SPANISH))
            .thenReturn(replacements);
        Mockito
            .when(replacementFindService.findReplacements(content2, WikipediaLanguage.SPANISH))
            .thenReturn(replacements);

        Optional<ArticleReview> review = articleService.findRandomArticleReview(options);
        Assertions.assertTrue(review.isPresent());
        Assertions.assertEquals(randomId, review.get().getId());

        review = articleService.findRandomArticleReview(options);
        Assertions.assertTrue(review.isPresent());
        Assertions.assertEquals(randomId2, review.get().getId());

        review = articleService.findRandomArticleReview(options);
        Assertions.assertFalse(review.isPresent());
    }

    @Test
    public void testArticleReviewWithSection() throws ReplacerException {
        final int sectionId = 1;

        // The article exists in Wikipedia
        Mockito
            .when(wikipediaService.getPageById(randomId, WikipediaLanguage.SPANISH))
            .thenReturn(Optional.of(article));

        // The article contains replacements
        Mockito
            .when(replacementFindService.findReplacements(content, WikipediaLanguage.SPANISH))
            .thenReturn(replacements);

        // The article has sections
        ArticleReview sectionReview = articleService.buildArticleReview(article, replacements);
        sectionReview.setSection(sectionId);
        Mockito
            .when(sectionReviewService.findSectionReview(Mockito.any(ArticleReview.class)))
            .thenReturn(Optional.of(sectionReview));

        Optional<ArticleReview> review = articleService.getArticleReview(randomId, options);

        Assertions.assertTrue(review.isPresent());
        review.ifPresent(
            rev -> {
                Assertions.assertEquals(randomId, rev.getId());
                Assertions.assertEquals(replacements.size(), rev.getReplacements().size());
                Assertions.assertEquals(sectionId, rev.getSection().intValue());
            }
        );
    }

    @Test
    public void testArticleReviewWithNoSection() throws ReplacerException {
        // The article exists in Wikipedia
        Mockito
            .when(wikipediaService.getPageById(randomId, WikipediaLanguage.SPANISH))
            .thenReturn(Optional.of(article));

        // The article contains replacements
        Mockito
            .when(replacementFindService.findReplacements(content, WikipediaLanguage.SPANISH))
            .thenReturn(replacements);

        // The article has no sections
        Mockito
            .when(sectionReviewService.findSectionReview(Mockito.any(ArticleReview.class)))
            .thenReturn(Optional.empty());

        Optional<ArticleReview> review = articleService.getArticleReview(randomId, options);

        Assertions.assertTrue(review.isPresent());
        review.ifPresent(
            rev -> {
                Assertions.assertEquals(randomId, rev.getId());
                Assertions.assertEquals(replacements.size(), rev.getReplacements().size());
                Assertions.assertNull(rev.getSection());
            }
        );
    }
}
