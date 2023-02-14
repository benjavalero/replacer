package es.bvalero.replacer.review;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.index.PageIndexResult;
import es.bvalero.replacer.index.PageIndexService;
import es.bvalero.replacer.page.PageCountService;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageService;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.user.UserId;
import es.bvalero.replacer.user.UserRightsService;
import es.bvalero.replacer.wikipedia.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReviewTypeFinderTest {

    private final int randomId = 1;
    private final int randomId2 = 2;
    private final String content = "XYZ";
    private final String content2 = "Y";
    private final PageKey randomPageKey = PageKey.of(WikipediaLanguage.getDefault(), randomId);
    private final PageKey randomPageKey2 = PageKey.of(WikipediaLanguage.getDefault(), randomId2);
    private final WikipediaPage page = WikipediaPage
        .builder()
        .pageKey(randomPageKey)
        .namespace(WikipediaNamespace.ARTICLE)
        .title("Title1")
        .content(content)
        .lastUpdate(WikipediaTimestamp.now())
        .queryTimestamp(WikipediaTimestamp.now())
        .build();
    private final WikipediaPage page2 = WikipediaPage
        .builder()
        .pageKey(randomPageKey2)
        .namespace(WikipediaNamespace.ANNEX)
        .title("Title2")
        .content(content2)
        .lastUpdate(WikipediaTimestamp.now())
        .queryTimestamp(WikipediaTimestamp.now())
        .build();
    private final int offset = 1;
    private final Replacement replacement = Replacement
        .builder()
        .start(offset)
        .type(ReplacementType.of(ReplacementKind.SIMPLE, "Y"))
        .text("Y")
        .suggestions(List.of(Suggestion.ofNoComment("Z")))
        .build();
    private final List<Replacement> replacements = Collections.singletonList(replacement);
    private final UserId userId = UserId.of(WikipediaLanguage.getDefault(), "A");
    private final ReviewOptions options = ReviewOptions.ofType(userId, ReplacementKind.SIMPLE.getCode(), "Y");
    private final ReviewOptions options2 = ReviewOptions.ofType(userId, ReplacementKind.COMPOSED.getCode(), "B");

    @Mock
    private PageService pageService;

    @Mock
    private PageCountService pageCountService;

    @Mock
    private ReplacementService replacementService;

    @Mock
    private WikipediaPageRepository wikipediaPageRepository;

    @Mock
    private PageIndexService pageIndexService;

    @Mock
    private ReviewSectionFinder reviewSectionFinder;

    @Mock
    private UserRightsService userRightsService;

    @InjectMocks
    private ReviewTypeFinder pageReviewTypeSubtypeService;

    @BeforeEach
    public void setUp() {
        pageReviewTypeSubtypeService = new ReviewTypeFinder();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindRandomPageToReviewTypeNotFiltered() {
        // 1 result in DB
        when(pageService.findPagesToReviewByType(any(WikipediaLanguage.class), any(ReplacementType.class), anyInt()))
            .thenReturn(Collections.singletonList(randomPageKey))
            .thenReturn(Collections.emptyList());

        // The page exists in Wikipedia
        when(wikipediaPageRepository.findByKey(randomPageKey)).thenReturn(Optional.of(page));

        when(pageIndexService.indexPage(page)).thenReturn(PageIndexResult.ofIndexed(replacements));

        Optional<Review> review = pageReviewTypeSubtypeService.findRandomPageReview(options2);

        verify(pageIndexService).indexPage(page);

        assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomPageToReviewTypeFiltered() {
        // 1 result in DB
        when(pageService.findPagesToReviewByType(any(WikipediaLanguage.class), any(ReplacementType.class), anyInt()))
            .thenReturn(Collections.singletonList(randomPageKey));

        // The page exists in Wikipedia
        when(wikipediaPageRepository.findByKey(randomPageKey)).thenReturn(Optional.of(page));

        when(pageIndexService.indexPage(page)).thenReturn(PageIndexResult.ofIndexed(replacements));

        Optional<Review> review = pageReviewTypeSubtypeService.findRandomPageReview(options);

        verify(pageIndexService).indexPage(page);

        assertTrue(review.isPresent());
        assertEquals(randomId, review.get().getPage().getPageId());
    }

    @Test
    void testFindRandomPageToReviewNoTypeAndThenFiltered() {
        // 1. Find the random page 1 by type. In DB there exists also the page 2.
        // 2. Find the random page 2 by no type. The page 2 is supposed to be removed from all the caches.
        // 3. Find a random page by type. In DB there is no page.

        // 2 results in DB by type, no results the second time.
        when(pageService.findPagesToReviewByType(any(WikipediaLanguage.class), any(ReplacementType.class), anyInt()))
            .thenReturn(List.of(randomPageKey, randomPageKey2))
            .thenReturn(Collections.emptyList());
        // 1 result in DB by no type
        when(pageService.findPagesToReviewByNoType(any(WikipediaLanguage.class), anyInt()))
            .thenReturn(Collections.singletonList(randomPageKey2));

        // The pages exist in Wikipedia
        when(wikipediaPageRepository.findByKey(randomPageKey)).thenReturn(Optional.of(page));
        when(wikipediaPageRepository.findByKey(randomPageKey2)).thenReturn(Optional.of(page2));

        when(pageIndexService.indexPage(any(WikipediaPage.class))).thenReturn(PageIndexResult.ofIndexed(replacements));

        Optional<Review> review = pageReviewTypeSubtypeService.findRandomPageReview(options);
        assertTrue(review.isPresent());
        assertEquals(randomId, review.get().getPage().getPageId());

        review = pageReviewTypeSubtypeService.findRandomPageReview(options);
        assertTrue(review.isPresent());
        assertEquals(randomId2, review.get().getPage().getPageId());

        review = pageReviewTypeSubtypeService.findRandomPageReview(options);
        assertFalse(review.isPresent());
    }

    @Test
    void testPageReviewWithSection() {
        final int sectionId = 1;

        // The page exists in Wikipedia
        when(wikipediaPageRepository.findByKey(randomPageKey)).thenReturn(Optional.of(page));

        when(pageIndexService.indexPage(page)).thenReturn(PageIndexResult.ofIndexed(replacements));

        // Load the cache in order to find the total results
        pageReviewTypeSubtypeService.loadCache(options);

        // The page has sections
        WikipediaSection section = WikipediaSection
            .builder()
            .level(2)
            .index(sectionId)
            .byteOffset(0)
            .anchor("")
            .build();
        int numPending = 5;
        Review sectionReview = Review.of(page, section, replacements, numPending);
        when(reviewSectionFinder.findPageReviewSection(any(Review.class))).thenReturn(Optional.of(sectionReview));

        Optional<Review> review = pageReviewTypeSubtypeService.findPageReview(randomPageKey, options);

        assertTrue(review.isPresent());
        review.ifPresent(rev -> {
            assertEquals(randomId, rev.getPage().getPageId());
            assertEquals(replacements.size(), rev.getReplacements().size());
            assertNotNull(rev.getSection());
            assertEquals(sectionId, rev.getSection().getIndex());
            assertEquals(numPending, rev.getNumPending());
        });
    }

    @Test
    void testPageReviewWithNoSection() {
        // The page exists in Wikipedia
        when(wikipediaPageRepository.findByKey(randomPageKey)).thenReturn(Optional.of(page));

        when(pageIndexService.indexPage(page)).thenReturn(PageIndexResult.ofIndexed(replacements));

        // The page has no sections
        when(reviewSectionFinder.findPageReviewSection(any(Review.class))).thenReturn(Optional.empty());

        // Load the cache in order to find the total results
        pageReviewTypeSubtypeService.loadCache(options);

        Optional<Review> review = pageReviewTypeSubtypeService.findPageReview(randomPageKey, options);

        assertTrue(review.isPresent());
        review.ifPresent(rev -> {
            assertEquals(randomId, rev.getPage().getPageId());
            assertEquals(replacements.size(), rev.getReplacements().size());
            assertNull(rev.getSection());
        });
    }

    @Test
    void testFindReplacementFilteredAndReviewed() {
        // 1 result in DB
        when(pageService.findPagesToReviewByType(any(WikipediaLanguage.class), any(ReplacementType.class), anyInt()))
            .thenReturn(Collections.singletonList(randomPageKey))
            .thenReturn(Collections.emptyList());

        // The page exists in Wikipedia
        when(wikipediaPageRepository.findByKey(randomPageKey)).thenReturn(Optional.of(page));

        final Replacement replacement2 = Replacement
            .builder()
            .start(offset + 1)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "Z"))
            .text("Z")
            .suggestions(List.of(Suggestion.ofNoComment("z")))
            .build();
        when(pageIndexService.indexPage(page)).thenReturn(PageIndexResult.ofIndexed(List.of(replacement2)));

        Optional<Review> review = pageReviewTypeSubtypeService.findRandomPageReview(options);

        verify(pageIndexService).indexPage(page);

        assertTrue(review.isEmpty());
    }
}
