package es.bvalero.replacer.page.find;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementKind;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.page.PageRepository;
import es.bvalero.replacer.page.count.PageCountRepository;
import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.page.index.PageIndexService;
import es.bvalero.replacer.page.save.PageSaveRepository;
import es.bvalero.replacer.replacement.ReplacementSaveRepository;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.wikipedia.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewTypeFinderTest {

    private final int randomId = 1;
    private final int randomId2 = 2;
    private final String content = "XYZ";
    private final String content2 = "AYB";
    private final PageKey randomPageKey = PageKey.of(WikipediaLanguage.getDefault(), randomId);
    private final PageKey randomPageKey2 = PageKey.of(WikipediaLanguage.getDefault(), randomId2);
    private final WikipediaPage page = WikipediaPage.builder()
        .pageKey(randomPageKey)
        .namespace(WikipediaNamespace.ARTICLE)
        .title("Title1")
        .content(content)
        .lastUpdate(WikipediaTimestamp.now())
        .queryTimestamp(WikipediaTimestamp.now())
        .build();
    private final WikipediaPage page2 = WikipediaPage.builder()
        .pageKey(randomPageKey2)
        .namespace(WikipediaNamespace.ANNEX)
        .title("Title2")
        .content(content2)
        .lastUpdate(WikipediaTimestamp.now())
        .queryTimestamp(WikipediaTimestamp.now())
        .build();
    private final StandardType simpleType = StandardType.of(ReplacementKind.SIMPLE, "Y");
    private final StandardType composedType = StandardType.of(ReplacementKind.COMPOSED, "B");
    private final Replacement replacement = Replacement.of(
        1,
        "Y",
        StandardType.of(ReplacementKind.SIMPLE, "Y"),
        List.of(Suggestion.ofNoComment("Z")),
        content
    );
    private final List<Replacement> replacements = List.of(replacement);
    private final User user = User.buildTestUser();
    private final ReviewOptions options = ReviewOptions.ofType(user, simpleType);
    private final ReviewOptions options2 = ReviewOptions.ofType(user, composedType);

    // Dependency injection
    private WikipediaPageRepository wikipediaPageRepository;
    private PageIndexService pageIndexService;
    private PageRepository pageRepository;
    private PageSaveRepository pageSaveRepository;
    private ReviewSectionFinder reviewSectionFinder;
    private PageCountRepository pageCountRepository;
    private ReplacementSaveRepository replacementSaveRepository;

    private ReviewTypeFinder pageReviewTypeSubtypeService;

    @BeforeEach
    public void setUp() {
        wikipediaPageRepository = mock(WikipediaPageRepository.class);
        pageIndexService = mock(PageIndexService.class);
        pageRepository = mock(PageRepository.class);
        pageSaveRepository = mock(PageSaveRepository.class);
        reviewSectionFinder = mock(ReviewSectionFinder.class);
        pageCountRepository = mock(PageCountRepository.class);
        replacementSaveRepository = mock(ReplacementSaveRepository.class);
        pageReviewTypeSubtypeService = new ReviewTypeFinder(
            wikipediaPageRepository,
            pageIndexService,
            pageRepository,
            pageSaveRepository,
            reviewSectionFinder,
            pageCountRepository,
            replacementSaveRepository
        );
    }

    @Test
    void testFindRandomPageToReviewTypeNotFiltered() {
        // 1 result in DB
        when(
            pageCountRepository.countNotReviewedByType(any(WikipediaLanguage.class), any(StandardType.class))
        ).thenReturn(1);
        when(pageRepository.findNotReviewedByType(any(WikipediaLanguage.class), any(StandardType.class), anyInt()))
            .thenReturn(List.of(randomPageKey))
            .thenReturn(List.of());

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
        when(
            pageCountRepository.countNotReviewedByType(any(WikipediaLanguage.class), any(StandardType.class))
        ).thenReturn(1);
        when(
            pageRepository.findNotReviewedByType(any(WikipediaLanguage.class), any(StandardType.class), anyInt())
        ).thenReturn(List.of(randomPageKey));

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
        when(
            pageCountRepository.countNotReviewedByType(any(WikipediaLanguage.class), any(StandardType.class))
        ).thenReturn(2);
        when(pageRepository.findNotReviewedByType(any(WikipediaLanguage.class), any(StandardType.class), anyInt()))
            .thenReturn(List.of(randomPageKey, randomPageKey2))
            .thenReturn(List.of());
        // 1 result in DB by no type
        when(pageCountRepository.countNotReviewedByType(any(WikipediaLanguage.class), isNull())).thenReturn(1);
        when(pageRepository.findNotReviewedByType(any(WikipediaLanguage.class), isNull(), anyInt())).thenReturn(
            List.of(randomPageKey2)
        );

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
        WikipediaSection section = WikipediaSection.builder()
            .pageKey(page.getPageKey())
            .index(sectionId)
            .level(2)
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
        when(
            pageCountRepository.countNotReviewedByType(any(WikipediaLanguage.class), any(StandardType.class))
        ).thenReturn(1);
        when(pageRepository.findNotReviewedByType(any(WikipediaLanguage.class), any(StandardType.class), anyInt()))
            .thenReturn(List.of(randomPageKey))
            .thenReturn(List.of());

        // The page exists in Wikipedia
        when(wikipediaPageRepository.findByKey(randomPageKey)).thenReturn(Optional.of(page));

        final Replacement replacement2 = Replacement.of(
            2,
            "Z",
            StandardType.of(ReplacementKind.SIMPLE, "Z"),
            List.of(Suggestion.ofNoComment("z")),
            page.getContent()
        );
        when(pageIndexService.indexPage(page)).thenReturn(PageIndexResult.ofIndexed(List.of(replacement2)));

        Optional<Review> review = pageReviewTypeSubtypeService.findRandomPageReview(options);

        verify(pageIndexService).indexPage(page);

        assertTrue(review.isEmpty());
    }
}
