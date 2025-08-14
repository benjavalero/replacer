package es.bvalero.replacer.page.find;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.User;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.page.PageCountRepository;
import es.bvalero.replacer.page.PageRepository;
import es.bvalero.replacer.page.PageSaveRepository;
import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.page.index.PageIndexService;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewNoTypeFinderTest {

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
    private final Replacement replacement = Replacement.of(
        1,
        "Y",
        StandardType.DEGREES,
        List.of(Suggestion.ofNoComment("Z")),
        content
    );
    private final List<Replacement> replacements = List.of(replacement);
    private final User user = User.buildTestUser();
    private final ReviewOptions options = ReviewOptions.ofNoType(user);

    // Dependency injection
    private WikipediaPageRepository wikipediaPageRepository;
    private PageIndexService pageIndexService;
    private PageRepository pageRepository;
    private PageSaveRepository pageSaveRepository;
    private ReviewSectionFinder reviewSectionFinder;
    private PageCountRepository pageCountRepository;

    private ReviewNoTypeFinder pageReviewNoTypeService;

    @BeforeEach
    public void setUp() {
        wikipediaPageRepository = mock(WikipediaPageRepository.class);
        pageIndexService = mock(PageIndexService.class);
        pageRepository = mock(PageRepository.class);
        pageSaveRepository = mock(PageSaveRepository.class);
        reviewSectionFinder = mock(ReviewSectionFinder.class);
        pageCountRepository = mock(PageCountRepository.class);
        pageReviewNoTypeService = new ReviewNoTypeFinder(
            wikipediaPageRepository,
            pageIndexService,
            pageRepository,
            pageSaveRepository,
            reviewSectionFinder,
            pageCountRepository
        );
    }

    @Test
    void testFindRandomPageToReviewNoTypeNoResultInDb() {
        // No results in DB
        when(pageRepository.findNotReviewedByType(any(WikipediaLanguage.class), isNull(), anyInt())).thenReturn(
            List.of()
        );

        Optional<Review> review = pageReviewNoTypeService.findRandomPageReview(options);

        assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomPageToReviewNoTypeNotInWikipedia() {
        // 1 result in DB
        when(pageRepository.findNotReviewedByType(any(WikipediaLanguage.class), isNull(), anyInt()))
            .thenReturn(new ArrayList<>(Set.of(randomPageKey)))
            .thenReturn(List.of());

        // The page doesn't exist in Wikipedia
        when(wikipediaPageRepository.findByKey(randomPageKey)).thenReturn(Optional.empty());

        Optional<Review> review = pageReviewNoTypeService.findRandomPageReview(options);

        assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomPageToReviewNoTypeWithReplacements() {
        // 1 result in DB
        when(pageCountRepository.countNotReviewedByType(any(WikipediaLanguage.class), isNull())).thenReturn(1);
        when(pageRepository.findNotReviewedByType(any(WikipediaLanguage.class), isNull(), anyInt())).thenReturn(
            new ArrayList<>(Set.of(randomPageKey))
        );

        // The page exists in Wikipedia
        when(wikipediaPageRepository.findByKey(randomPageKey)).thenReturn(Optional.of(page));

        when(pageIndexService.indexPage(page)).thenReturn(PageIndexResult.ofIndexed(replacements));

        Optional<Review> review = pageReviewNoTypeService.findRandomPageReview(options);

        verify(pageIndexService).indexPage(page);

        assertTrue(review.isPresent());
        assertEquals(randomId, review.get().getPage().getPageId());
    }

    @Test
    void testFindRandomPageToReviewNoTypeNoReplacements() {
        // 1 result in DB
        when(pageCountRepository.countNotReviewedByType(any(WikipediaLanguage.class), isNull())).thenReturn(1);
        when(pageRepository.findNotReviewedByType(any(WikipediaLanguage.class), isNull(), anyInt()))
            .thenReturn(new ArrayList<>(Set.of(randomPageKey)))
            .thenReturn(List.of());

        // The page exists in Wikipedia
        when(wikipediaPageRepository.findByKey(randomPageKey)).thenReturn(Optional.of(page));

        // The page doesn't contain replacements
        when(pageIndexService.indexPage(page)).thenReturn(PageIndexResult.ofNotIndexed());

        Optional<Review> review = pageReviewNoTypeService.findRandomPageReview(options);

        verify(pageIndexService).indexPage(page);

        assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomPageToReviewNoTypeSecondResult() {
        // 2 results in DB
        when(pageCountRepository.countNotReviewedByType(any(WikipediaLanguage.class), isNull())).thenReturn(2);
        when(pageRepository.findNotReviewedByType(any(WikipediaLanguage.class), isNull(), anyInt())).thenReturn(
            List.of(randomPageKey2, randomPageKey)
        );

        // Only page 1 exists in Wikipedia
        when(wikipediaPageRepository.findByKey(randomPageKey2)).thenReturn(Optional.empty());
        when(wikipediaPageRepository.findByKey(randomPageKey)).thenReturn(Optional.of(page));

        when(pageIndexService.indexPage(page)).thenReturn(PageIndexResult.ofIndexed(replacements));

        Optional<Review> review = pageReviewNoTypeService.findRandomPageReview(options);

        verify(pageIndexService).indexPage(page);

        assertTrue(review.isPresent());
        assertEquals(randomId, review.get().getPage().getPageId());
    }

    @Test
    void testReviewAfterWhenCacheIsEmpty() {
        // When a page to review is retrieved and presented to the user, is removed from the cache.
        // If user chooses to Review After, a new page to review is retrieved and presented.
        // When the cache is empty, it should mean that there are no more pages to review.
        // However, the page marked to review after is in fact pending to review and should be retrieved.

        // First load of cache
        when(pageCountRepository.countNotReviewedByType(any(WikipediaLanguage.class), isNull())).thenReturn(1);
        when(pageRepository.findNotReviewedByType(any(WikipediaLanguage.class), isNull(), anyInt())).thenReturn(
            List.of(randomPageKey)
        );

        when(wikipediaPageRepository.findByKey(randomPageKey)).thenReturn(Optional.of(page));

        when(pageIndexService.indexPage(page)).thenReturn(PageIndexResult.ofIndexed(replacements));

        Optional<Review> review = pageReviewNoTypeService.findRandomPageReview(options);

        verify(pageIndexService).indexPage(page);

        assertTrue(review.isPresent());
        assertEquals(randomId, review.get().getPage().getPageId());

        // At this point, the user marks the page to Review After.
        // The page should be retrieved when asked for a new page.

        review = pageReviewNoTypeService.findRandomPageReview(options);

        assertTrue(review.isPresent());
        assertEquals(randomId, review.get().getPage().getPageId());
    }
}
