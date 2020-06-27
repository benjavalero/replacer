package es.bvalero.replacer.page;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementCountService;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.replacement.ReplacementRepository;
import es.bvalero.replacer.wikipedia.*;
import java.time.LocalDate;
import java.util.*;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

@SpringBootTest(classes = XmlConfiguration.class)
class PageReviewTypeSubtypeServiceTest {
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
    private final PageReviewOptions options = PageReviewOptions.ofTypeSubtype(WikipediaLanguage.SPANISH, "X", "Y");
    private final PageReviewOptions options2 = PageReviewOptions.ofTypeSubtype(WikipediaLanguage.SPANISH, "A", "B");

    @Resource
    private List<String> ignorableTemplates;

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
    private PageReviewTypeSubtypeService articleService;

    @BeforeEach
    public void setUp() {
        articleService = new PageReviewTypeSubtypeService();
        articleService.setIgnorableTemplates(ignorableTemplates);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindRandomArticleToReviewTypeNotFiltered() throws ReplacerException {
        // 1 result in DB
        Mockito
            .when(
                replacementRepository.findRandomPageIdsToReviewByTypeAndSubtype(
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

        Optional<PageReview> review = articleService.findRandomPageReview(options2);

        Mockito
            .verify(replacementIndexService, Mockito.times(1))
            .indexPageReplacements(Mockito.eq(randomId), Mockito.any(WikipediaLanguage.class), Mockito.anyList());

        Assertions.assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomArticleToReviewTypeFiltered() throws ReplacerException {
        // 1 result in DB
        Mockito
            .when(
                replacementRepository.findRandomPageIdsToReviewByTypeAndSubtype(
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

        Optional<PageReview> review = articleService.findRandomPageReview(options);

        Mockito
            .verify(replacementIndexService, Mockito.times(1))
            .indexPageReplacements(Mockito.eq(randomId), Mockito.any(WikipediaLanguage.class), Mockito.anyList());

        Assertions.assertTrue(review.isPresent());
        Assertions.assertEquals(randomId, review.get().getId());
    }

    @Test
    void testFindRandomArticleToReviewNoTypeAndThenFiltered() throws ReplacerException {
        // 1. Find the random article 1 by type. In DB there exists also the article 2.
        // 2. Find the random article 2 by no type. The article 2 is supposed to be removed from all the caches.
        // 3. Find a random article by type. In DB there is no article.

        // 2 results in DB by type, no results the second time.
        Mockito
            .when(
                replacementRepository.findRandomPageIdsToReviewByTypeAndSubtype(
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
                replacementRepository.findRandomPageIdsToReview(
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

        Optional<PageReview> review = articleService.findRandomPageReview(options);
        Assertions.assertTrue(review.isPresent());
        Assertions.assertEquals(randomId, review.get().getId());

        review = articleService.findRandomPageReview(options);
        Assertions.assertTrue(review.isPresent());
        Assertions.assertEquals(randomId2, review.get().getId());

        review = articleService.findRandomPageReview(options);
        Assertions.assertFalse(review.isPresent());
    }

    @Test
    void testPageReviewWithSection() throws ReplacerException {
        final int sectionId = 1;

        // The article exists in Wikipedia
        Mockito
            .when(wikipediaService.getPageById(randomId, WikipediaLanguage.SPANISH))
            .thenReturn(Optional.of(article));

        // The article contains replacements
        Mockito
            .when(replacementFindService.findReplacements(content, WikipediaLanguage.SPANISH))
            .thenReturn(replacements);

        // Load the cache in order to find the total results
        articleService.loadCache(options);

        // The article has sections
        PageReview sectionReview = articleService.buildPageReview(article, replacements, options);
        sectionReview.setSection(sectionId);
        Mockito
            .when(sectionReviewService.findSectionReview(Mockito.any(PageReview.class)))
            .thenReturn(Optional.of(sectionReview));

        Optional<PageReview> review = articleService.getPageReview(randomId, options);

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
    void testPageReviewWithNoSection() throws ReplacerException {
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
            .when(sectionReviewService.findSectionReview(Mockito.any(PageReview.class)))
            .thenReturn(Optional.empty());

        // Load the cache in order to find the total results
        articleService.loadCache(options);

        Optional<PageReview> review = articleService.getPageReview(randomId, options);

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
