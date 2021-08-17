package es.bvalero.replacer.page;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.finder.replacement.ReplacementSuggestion;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import es.bvalero.replacer.page.index.IndexablePageValidator;
import es.bvalero.replacer.page.index.PageIndexHelper;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.PageRequest;

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
        .lastUpdate(LocalDateTime.now())
        .queryTimestamp(LocalDateTime.now())
        .build();
    private final WikipediaPage page2 = WikipediaPage
        .builder()
        .id(randomId2)
        .lang(WikipediaLanguage.SPANISH)
        .namespace(WikipediaNamespace.ANNEX)
        .content(content2)
        .lastUpdate(LocalDateTime.now())
        .queryTimestamp(LocalDateTime.now())
        .build();
    private final int offset = 1;
    private final Replacement replacement = Replacement
        .builder()
        .start(offset)
        .text("Y")
        .type(ReplacementType.DATE)
        .suggestions(List.of(ReplacementSuggestion.ofNoComment("Z")))
        .build();
    private final List<Replacement> replacements = Collections.singletonList(replacement);
    private final PageReviewOptions options = PageReviewOptions.ofNoType();

    @Mock
    private ReplacementService replacementService;

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private PageIndexHelper pageIndexHelper;

    @Mock
    private ReplacementFinderService replacementFinderService;

    @Mock
    private SectionReviewService sectionReviewService;

    @Mock
    private IndexablePageValidator indexablePageValidator;

    @InjectMocks
    private PageReviewNoTypeService pageReviewNoTypeService;

    @BeforeEach
    public void setUp() {
        pageReviewNoTypeService = new PageReviewNoTypeService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindRandomPageToReviewNoTypeNoResultInDb() {
        // No results in DB
        Mockito
            .when(
                replacementService.findPageIdsToBeReviewed(
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
                replacementService.findPageIdsToBeReviewed(
                    Mockito.any(WikipediaLanguage.class),
                    Mockito.anyLong(),
                    Mockito.any(PageRequest.class)
                )
            )
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)))
            .thenReturn(Collections.emptyList());

        // The page doesn't exist in Wikipedia
        Mockito.when(wikipediaService.getPageById(WikipediaLanguage.SPANISH, randomId)).thenReturn(Optional.empty());

        Optional<PageReview> review = pageReviewNoTypeService.findRandomPageReview(options);

        Assertions.assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomPageToReviewNoTypeWithReplacements() throws ReplacerException {
        // 1 result in DB
        Mockito
            .when(
                replacementService.findPageIdsToBeReviewed(
                    Mockito.any(WikipediaLanguage.class),
                    Mockito.anyLong(),
                    Mockito.any(PageRequest.class)
                )
            )
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)));

        // The page exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(WikipediaLanguage.SPANISH, randomId)).thenReturn(Optional.of(page));

        // The page contains replacements
        Mockito
            .when(replacementFinderService.findList(pageReviewNoTypeService.convertToFinderPage(page)))
            .thenReturn(replacements);

        Optional<PageReview> review = pageReviewNoTypeService.findRandomPageReview(options);

        Mockito
            .verify(pageIndexHelper, Mockito.times(1))
            .indexPageReplacements(Mockito.eq(pageReviewNoTypeService.convertToIndexablePage(page)), Mockito.anyList());

        Assertions.assertTrue(review.isPresent());
        Assertions.assertEquals(randomId, review.get().getPage().getId());
    }

    @Test
    void testFindRandomPageToReviewNoTypeNoReplacements() throws ReplacerException {
        // 1 result in DB
        Mockito
            .when(
                replacementService.findPageIdsToBeReviewed(
                    Mockito.any(WikipediaLanguage.class),
                    Mockito.anyLong(),
                    Mockito.any(PageRequest.class)
                )
            )
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)))
            .thenReturn(Collections.emptyList());

        // The page exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(WikipediaLanguage.SPANISH, randomId)).thenReturn(Optional.of(page));

        // The page doesn't contain replacements
        List<Replacement> noPageReplacements = Collections.emptyList();
        Mockito
            .when(replacementFinderService.findList(pageReviewNoTypeService.convertToFinderPage(page)))
            .thenReturn(noPageReplacements);

        Optional<PageReview> review = pageReviewNoTypeService.findRandomPageReview(options);

        Mockito
            .verify(pageIndexHelper, Mockito.times(1))
            .indexPageReplacements(pageReviewNoTypeService.convertToIndexablePage(page), Collections.emptyList());

        Assertions.assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomPageToReviewNoTypeSecondResult() throws ReplacerException {
        // 2 results in DB
        Mockito
            .when(
                replacementService.findPageIdsToBeReviewed(
                    Mockito.any(WikipediaLanguage.class),
                    Mockito.anyLong(),
                    Mockito.any(PageRequest.class)
                )
            )
            .thenReturn(new ArrayList<>(Arrays.asList(randomId, randomId2)));

        // Only the page 2 exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(WikipediaLanguage.SPANISH, randomId)).thenReturn(Optional.empty());
        Mockito.when(wikipediaService.getPageById(WikipediaLanguage.SPANISH, randomId2)).thenReturn(Optional.of(page2));

        // The page contains replacements
        Mockito
            .when(replacementFinderService.findList(pageReviewNoTypeService.convertToFinderPage(page2)))
            .thenReturn(replacements);

        Optional<PageReview> review = pageReviewNoTypeService.findRandomPageReview(options);

        Mockito
            .verify(pageIndexHelper, Mockito.times(1))
            .indexPageReplacements(
                Mockito.eq(pageReviewNoTypeService.convertToIndexablePage(page2)),
                Mockito.anyList()
            );

        Assertions.assertTrue(review.isPresent());
        Assertions.assertEquals(randomId2, review.get().getPage().getId());
    }
}
