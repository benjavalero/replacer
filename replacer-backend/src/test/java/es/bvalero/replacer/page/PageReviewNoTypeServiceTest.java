package es.bvalero.replacer.page;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementDao;
import es.bvalero.replacer.replacement.ReplacementIndexService;
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
class PageReviewNoTypeServiceTest {
    private final int randomId = 1;
    private final int randomId2 = 2;
    private final String content = "XYZ";
    private final String content2 = "Y";
    private final WikipediaPage page = WikipediaPage
        .builder()
        .id(randomId)
        .lang(WikipediaLanguage.SPANISH)
        .namespace(WikipediaNamespace.ARTICLE)
        .content(content)
        .lastUpdate(LocalDate.now())
        .build();
    private final WikipediaPage page2 = WikipediaPage
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
    private final PageReviewOptions options = PageReviewOptions.ofNoType(WikipediaLanguage.SPANISH);

    @Resource
    private List<String> ignorableTemplates;

    @Mock
    private ReplacementDao replacementDao;

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
    private PageReviewNoTypeService pageReviewNoTypeService;

    @BeforeEach
    public void setUp() {
        pageReviewNoTypeService = new PageReviewNoTypeService();
        pageReviewNoTypeService.setIgnorableTemplates(ignorableTemplates);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindRandomPageToReviewNoTypeNoResultInDb() {
        // No results in DB
        Mockito
            .when(
                replacementDao.findPageIdsToBeReviewed(
                    Mockito.any(WikipediaLanguage.class),
                    Mockito.anyLong(),
                    Mockito.any(PageRequest.class)
                )
            )
            .thenReturn(Collections.emptyList());

        Optional<PageReview> review = pageReviewNoTypeService.findRandomPageReview(options);

        Assertions.assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomPageToReviewNoTypeNotInWikipedia() throws ReplacerException {
        // 1 result in DB
        Mockito
            .when(
                replacementDao.findPageIdsToBeReviewed(
                    Mockito.any(WikipediaLanguage.class),
                    Mockito.anyLong(),
                    Mockito.any(PageRequest.class)
                )
            )
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)))
            .thenReturn(Collections.emptyList());

        // The page doesn't exist in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId, WikipediaLanguage.SPANISH)).thenReturn(Optional.empty());

        Optional<PageReview> review = pageReviewNoTypeService.findRandomPageReview(options);

        Assertions.assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomPageToReviewNoTypeWithReplacements() throws ReplacerException {
        // 1 result in DB
        Mockito
            .when(
                replacementDao.findPageIdsToBeReviewed(
                    Mockito.any(WikipediaLanguage.class),
                    Mockito.anyLong(),
                    Mockito.any(PageRequest.class)
                )
            )
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)));

        // The page exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId, WikipediaLanguage.SPANISH)).thenReturn(Optional.of(page));

        // The page contains replacements
        Mockito
            .when(replacementFindService.findReplacements(content, WikipediaLanguage.SPANISH))
            .thenReturn(replacements);

        Optional<PageReview> review = pageReviewNoTypeService.findRandomPageReview(options);

        Mockito
            .verify(replacementIndexService, Mockito.times(1))
            .indexPageReplacements(Mockito.eq(page), Mockito.anyList());

        Assertions.assertTrue(review.isPresent());
        Assertions.assertEquals(randomId, review.get().getId());
    }

    @Test
    void testFindRandomPageToReviewNoTypeNoReplacements() throws ReplacerException {
        // 1 result in DB
        Mockito
            .when(
                replacementDao.findPageIdsToBeReviewed(
                    Mockito.any(WikipediaLanguage.class),
                    Mockito.anyLong(),
                    Mockito.any(PageRequest.class)
                )
            )
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)))
            .thenReturn(Collections.emptyList());

        // The page exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId, WikipediaLanguage.SPANISH)).thenReturn(Optional.of(page));

        // The page doesn't contain replacements
        List<Replacement> noPageReplacements = Collections.emptyList();
        Mockito
            .when(replacementFindService.findReplacements(content, WikipediaLanguage.SPANISH))
            .thenReturn(noPageReplacements);

        Optional<PageReview> review = pageReviewNoTypeService.findRandomPageReview(options);

        Mockito
            .verify(replacementIndexService, Mockito.times(1))
            .indexPageReplacements(page, Collections.emptyList());

        Assertions.assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomPageToReviewNoTypeSecondResult() throws ReplacerException {
        // 2 results in DB
        Mockito
            .when(
                replacementDao.findPageIdsToBeReviewed(
                    Mockito.any(WikipediaLanguage.class),
                    Mockito.anyLong(),
                    Mockito.any(PageRequest.class)
                )
            )
            .thenReturn(new ArrayList<>(Arrays.asList(randomId, randomId2)));

        // Only the page 2 exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(randomId, WikipediaLanguage.SPANISH)).thenReturn(Optional.empty());
        Mockito.when(wikipediaService.getPageById(randomId2, WikipediaLanguage.SPANISH)).thenReturn(Optional.of(page2));

        // The page contains replacements
        Mockito
            .when(replacementFindService.findReplacements(content2, WikipediaLanguage.SPANISH))
            .thenReturn(replacements);

        Optional<PageReview> review = pageReviewNoTypeService.findRandomPageReview(options);

        Mockito
            .verify(replacementIndexService, Mockito.times(1))
            .indexPageReplacements(Mockito.eq(page2), Mockito.anyList());

        Assertions.assertTrue(review.isPresent());
        Assertions.assertEquals(randomId2, review.get().getId());
    }
}
