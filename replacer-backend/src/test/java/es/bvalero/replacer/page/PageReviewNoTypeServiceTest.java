package es.bvalero.replacer.page;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.page.index.PageIndexStatus;
import es.bvalero.replacer.page.index.PageIndexer;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;

class PageReviewNoTypeServiceTest {

    private final int randomId = 1;
    private final int randomId2 = 2;
    private final String content = "XYZ";
    private final String content2 = "Y";
    private final WikipediaPageId randomPageId = WikipediaPageId.of(WikipediaLanguage.getDefault(), randomId);
    private final WikipediaPageId randomPageId2 = WikipediaPageId.of(WikipediaLanguage.getDefault(), randomId2);
    private final WikipediaPage page = WikipediaPage
        .builder()
        .id(randomPageId)
        .namespace(WikipediaNamespace.ARTICLE)
        .title("Title1")
        .content(content)
        .lastUpdate(LocalDateTime.now())
        .build();
    private final WikipediaPage page2 = WikipediaPage
        .builder()
        .id(randomPageId2)
        .namespace(WikipediaNamespace.ANNEX)
        .title("Title2")
        .content(content2)
        .lastUpdate(LocalDateTime.now())
        .build();
    private final int offset = 1;
    private final Replacement replacement = Replacement
        .builder()
        .start(offset)
        .text("Y")
        .type(ReplacementType.DATE)
        .subtype("Año con punto")
        .suggestions(List.of(Suggestion.ofNoComment("Z")))
        .build();
    private final List<Replacement> replacements = Collections.singletonList(replacement);
    private final PageReviewOptions options = PageReviewOptions.ofNoType();

    @Mock
    private ReplacementService replacementService;

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private PageIndexer pageIndexer;

    @Mock
    private SectionReviewService sectionReviewService;

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
        when(
            replacementService.findPageIdsToBeReviewed(any(WikipediaLanguage.class), anyLong(), any(PageRequest.class))
        )
            .thenReturn(Collections.emptyList());

        Optional<PageReview> review = pageReviewNoTypeService.findRandomPageReview(options);

        assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomPageToReviewNoTypeNotInWikipedia() throws ReplacerException {
        // 1 result in DB
        when(
            replacementService.findPageIdsToBeReviewed(any(WikipediaLanguage.class), anyLong(), any(PageRequest.class))
        )
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)))
            .thenReturn(Collections.emptyList());

        // The page doesn't exist in Wikipedia
        when(wikipediaService.getPageById(randomPageId)).thenReturn(Optional.empty());

        Optional<PageReview> review = pageReviewNoTypeService.findRandomPageReview(options);

        assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomPageToReviewNoTypeWithReplacements() throws ReplacerException {
        // 1 result in DB
        when(
            replacementService.findPageIdsToBeReviewed(any(WikipediaLanguage.class), anyLong(), any(PageRequest.class))
        )
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)));

        // The page exists in Wikipedia
        when(wikipediaService.getPageById(randomPageId)).thenReturn(Optional.of(page));

        when(pageIndexer.indexPageReplacements(page))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_INDEXED, replacements));

        Optional<PageReview> review = pageReviewNoTypeService.findRandomPageReview(options);

        verify(pageIndexer).indexPageReplacements(page);

        assertTrue(review.isPresent());
        assertEquals(randomId, review.get().getPage().getId());
    }

    @Test
    void testFindRandomPageToReviewNoTypeNoReplacements() throws ReplacerException {
        // 1 result in DB
        when(
            replacementService.findPageIdsToBeReviewed(any(WikipediaLanguage.class), anyLong(), any(PageRequest.class))
        )
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)))
            .thenReturn(Collections.emptyList());

        // The page exists in Wikipedia
        when(wikipediaService.getPageById(randomPageId)).thenReturn(Optional.of(page));

        // The page doesn't contain replacements
        List<Replacement> noPageReplacements = Collections.emptyList();
        when(pageIndexer.indexPageReplacements(page))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_INDEXED, noPageReplacements));

        Optional<PageReview> review = pageReviewNoTypeService.findRandomPageReview(options);

        verify(pageIndexer).indexPageReplacements(page);

        assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomPageToReviewNoTypeSecondResult() throws ReplacerException {
        // 2 results in DB
        when(
            replacementService.findPageIdsToBeReviewed(any(WikipediaLanguage.class), anyLong(), any(PageRequest.class))
        )
            .thenReturn(new ArrayList<>(Arrays.asList(randomId, randomId2)));

        // Only the page 2 exists in Wikipedia
        when(wikipediaService.getPageById(randomPageId)).thenReturn(Optional.empty());
        when(wikipediaService.getPageById(randomPageId2)).thenReturn(Optional.of(page2));

        when(pageIndexer.indexPageReplacements(page2))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_INDEXED, replacements));

        Optional<PageReview> review = pageReviewNoTypeService.findRandomPageReview(options);

        verify(pageIndexer).indexPageReplacements(page2);

        assertTrue(review.isPresent());
        assertEquals(randomId2, review.get().getPage().getId());
    }
}
