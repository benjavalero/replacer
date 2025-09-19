package es.bvalero.replacer.review;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.User;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.index.PageIndexApi;
import es.bvalero.replacer.index.PageIndexResult;
import es.bvalero.replacer.page.PageRepository;
import es.bvalero.replacer.page.PageSaveRepository;
import es.bvalero.replacer.replacement.CustomRepository;
import es.bvalero.replacer.wikipedia.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewCustomFinderTest {

    private static final int CACHE_SIZE = 3;
    private static final Collection<WikipediaNamespace> NAMESPACES = Set.of(WikipediaNamespace.getDefault());
    private static final User user = User.buildTestUser();
    private static final WikipediaLanguage lang = user.getId().getLang();

    private static final String replacement = "R";
    private static final String suggestion = "S";
    private static final ReviewOptions options = ReviewOptions.ofCustom(user, replacement, true, suggestion);
    private static final CustomType customType = options.getCustomType();

    // Dependency injection
    private WikipediaPageRepository wikipediaPageRepository;
    private PageIndexApi pageIndexApi;
    private PageRepository pageRepository;
    private PageSaveRepository pageSaveRepository;
    private ReviewSectionFinder reviewSectionFinder;
    private CustomRepository customRepository;
    private CustomReplacementFindApi customReplacementFindApi;

    private ReviewCustomFinder pageReviewCustomService;

    @BeforeEach
    public void setUp() {
        wikipediaPageRepository = mock(WikipediaPageRepository.class);
        pageIndexApi = mock(PageIndexApi.class);
        pageRepository = mock(PageRepository.class);
        pageSaveRepository = mock(PageSaveRepository.class);
        reviewSectionFinder = mock(ReviewSectionFinder.class);
        customRepository = mock(CustomRepository.class);
        customReplacementFindApi = mock(CustomReplacementFindApi.class);
        pageReviewCustomService = new ReviewCustomFinder(
            wikipediaPageRepository,
            pageIndexApi,
            pageRepository,
            pageSaveRepository,
            reviewSectionFinder,
            customRepository,
            customReplacementFindApi
        );
        pageReviewCustomService.setCacheSize(CACHE_SIZE);
        pageReviewCustomService.setIndexableNamespaces(
            NAMESPACES.stream().map(WikipediaNamespace::getValue).collect(Collectors.toUnmodifiableSet())
        );
    }

    private WikipediaPage buildWikipediaPage(int pageId, String content) {
        return WikipediaPage.builder()
            .pageKey(PageKey.of(WikipediaLanguage.getDefault(), pageId))
            .namespace(WikipediaNamespace.getDefault())
            .title("Title")
            .content(content)
            .lastUpdate(WikipediaTimestamp.now())
            .queryTimestamp(WikipediaTimestamp.now())
            .build();
    }

    private WikipediaSearchRequest buildWikipediaSearchRequest(String replacement) {
        return buildWikipediaSearchRequest(replacement, 0);
    }

    private WikipediaSearchRequest buildWikipediaSearchRequest(String replacement, int offset) {
        return WikipediaSearchRequest.builder()
            .lang(WikipediaLanguage.getDefault())
            .namespaces(NAMESPACES)
            .text(replacement)
            .caseSensitive(true)
            .offset(offset)
            .limit(CACHE_SIZE)
            .build();
    }

    @Test
    void testNoResults() {
        // No results in Wikipedia Search ==> Return an empty review

        when(
            wikipediaPageRepository.findByContent(any(WikipediaSearchRequest.class), any(AccessToken.class))
        ).thenReturn(WikipediaSearchResult.ofEmpty());

        Optional<Review> review = pageReviewCustomService.findRandomPageReview(options);

        assertTrue(review.isEmpty());

        verify(wikipediaPageRepository).findByContent(buildWikipediaSearchRequest(replacement), user.getAccessToken());
        verify(customRepository, never()).findPagesReviewed(any(WikipediaLanguage.class), any(CustomType.class));
    }

    @Test
    void testResultAlreadyReviewed() {
        // Search in Wikipedia returns a result which is already reviewed in database
        // ==> Return an empty review

        final int pageId = 123;
        final WikipediaSearchResult searchResult = WikipediaSearchResult.builder().total(1).pageId(pageId).build();

        // Mocks
        when(
            wikipediaPageRepository.findByContent(any(WikipediaSearchRequest.class), any(AccessToken.class))
        ).thenReturn(searchResult);
        when(customRepository.findPagesReviewed(any(WikipediaLanguage.class), any(CustomType.class))).thenReturn(
            List.of(PageKey.of(WikipediaLanguage.getDefault(), pageId))
        );

        // Only one call
        Optional<Review> review = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaPageRepository).findByContent(buildWikipediaSearchRequest(replacement), user.getAccessToken());
        verify(customRepository).findPagesReviewed(lang, customType);
    }

    @Test
    void testResultWithReview() {
        // Search in Wikipedia returns a result which is not reviewed yet
        // ==> Return a review for that result
        // The user reviews the page so there are no more results to review
        // ==> Return an empty review

        final int pageId = 123;
        final String content = "A R";
        final Replacement customRep = Replacement.of(
            2,
            replacement,
            customType,
            List.of(Suggestion.ofNoComment(suggestion))
        );
        final WikipediaSearchResult searchResult = WikipediaSearchResult.builder().total(1).pageId(pageId).build();
        final PageKey pageKey = PageKey.of(lang, pageId);
        final WikipediaPage page = buildWikipediaPage(pageId, content);

        // Mocks
        when(
            wikipediaPageRepository.findByContent(any(WikipediaSearchRequest.class), any(AccessToken.class))
        ).thenReturn(searchResult);
        when(customRepository.findPagesReviewed(any(WikipediaLanguage.class), any(CustomType.class))).thenReturn(
            List.of()
        );
        when(wikipediaPageRepository.findByKey(any(PageKey.class), any(AccessToken.class))).thenReturn(
            Optional.of(page)
        );
        when(pageIndexApi.indexPage(page)).thenReturn(PageIndexResult.ofIndexed());
        when(
            customReplacementFindApi.findCustomReplacements(
                any(FinderPage.class),
                any(CustomReplacementFindRequest.class)
            )
        ).thenReturn(Stream.of(customRep).collect(Collectors.toCollection(TreeSet::new)));

        // First call
        Optional<Review> review1 = pageReviewCustomService.findRandomPageReview(options);
        assertFalse(review1.isEmpty());
        review1.ifPresent(r -> {
            assertEquals(lang, r.getPage().getPageKey().getLang());
            assertEquals(pageId, r.getPage().getPageId());
            assertEquals(content, r.getPage().getContent());
            assertEquals(1, r.getNumPending());
        });

        // Second call
        Optional<Review> review2 = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review2.isEmpty());

        // Verifications
        verify(wikipediaPageRepository).findByContent(buildWikipediaSearchRequest(replacement), user.getAccessToken());
        verify(customRepository).findPagesReviewed(lang, customType);
        verify(wikipediaPageRepository).findByKey(pageKey, user.getAccessToken());
    }

    @Test
    void testResultWithNoReplacements() {
        // Search in Wikipedia returns a result which is not reviewed yet but the page has no replacements
        // ==> Return an empty review
        // We don't insert any fake replacement in database.
        // Custom replacements are not re-indexed, so we wouldn't detect future changes in the page.

        final int pageId = 123;
        final String content = "A R";
        final WikipediaSearchResult searchResult = WikipediaSearchResult.builder().total(1).pageId(pageId).build();
        final PageKey pageKey = PageKey.of(lang, pageId);
        final WikipediaPage page = buildWikipediaPage(pageId, content);

        // Mocks
        when(
            wikipediaPageRepository.findByContent(any(WikipediaSearchRequest.class), any(AccessToken.class))
        ).thenReturn(searchResult);
        when(customRepository.findPagesReviewed(any(WikipediaLanguage.class), any(CustomType.class))).thenReturn(
            List.of()
        );
        when(wikipediaPageRepository.findByKey(any(PageKey.class), any(AccessToken.class))).thenReturn(
            Optional.of(page)
        );
        when(pageIndexApi.indexPage(page)).thenReturn(PageIndexResult.ofIndexed());
        when(
            customReplacementFindApi.findCustomReplacements(
                any(FinderPage.class),
                any(CustomReplacementFindRequest.class)
            )
        ).thenReturn(new TreeSet<>());

        // Only call
        Optional<Review> review = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaPageRepository).findByContent(buildWikipediaSearchRequest(replacement), user.getAccessToken());
        verify(customRepository).findPagesReviewed(lang, customType);
        verify(wikipediaPageRepository).findByKey(pageKey, user.getAccessToken());
    }

    @Test
    void testTwoResultsFirstReviewed() {
        // Search in Wikipedia returns two results with the first one reviewed in database
        // ==> Return a review for the second result

        final int pageId1 = 123;
        final int pageId2 = 456;
        final String content = "A R";
        final Replacement customRep = Replacement.of(
            2,
            replacement,
            customType,
            List.of(Suggestion.ofNoComment(suggestion))
        );
        final WikipediaSearchResult searchResult = WikipediaSearchResult.builder()
            .total(2)
            .pageId(pageId1)
            .pageId(pageId2)
            .build();
        final PageKey pageKey2 = PageKey.of(lang, pageId2);
        final WikipediaPage page = buildWikipediaPage(pageId2, content);

        // Mocks
        when(
            wikipediaPageRepository.findByContent(any(WikipediaSearchRequest.class), any(AccessToken.class))
        ).thenReturn(searchResult);
        when(customRepository.findPagesReviewed(any(WikipediaLanguage.class), any(CustomType.class))).thenReturn(
            List.of(PageKey.of(WikipediaLanguage.getDefault(), pageId1))
        );
        when(wikipediaPageRepository.findByKey(any(PageKey.class), any(AccessToken.class))).thenReturn(
            Optional.of(page)
        );
        when(pageIndexApi.indexPage(page)).thenReturn(PageIndexResult.ofIndexed());
        when(
            customReplacementFindApi.findCustomReplacements(
                any(FinderPage.class),
                any(CustomReplacementFindRequest.class)
            )
        ).thenReturn(Stream.of(customRep).collect(Collectors.toCollection(TreeSet::new)));

        // Only call
        Optional<Review> review = pageReviewCustomService.findRandomPageReview(options);
        assertFalse(review.isEmpty());
        review.ifPresent(r -> {
            assertEquals(lang, r.getPage().getPageKey().getLang());
            assertEquals(pageId2, r.getPage().getPageId());
            assertEquals(content, r.getPage().getContent());
            assertEquals(1, r.getNumPending());
        });

        // Verifications
        verify(wikipediaPageRepository).findByContent(buildWikipediaSearchRequest(replacement), user.getAccessToken());
        verify(customRepository).findPagesReviewed(lang, customType);
        verify(wikipediaPageRepository).findByKey(pageKey2, user.getAccessToken());
    }

    @Test
    void testSeveralResultsAllReviewed() {
        // Search in Wikipedia returns 4 results (pagination = 3) all of them reviewed in database
        // so we perform two calls to Wikipedia search
        // ==> Return an empty review

        // Mocks
        when(wikipediaPageRepository.findByContent(any(WikipediaSearchRequest.class), any(AccessToken.class)))
            .thenReturn(WikipediaSearchResult.builder().total(4).pageId(12).pageId(23).pageId(34).build())
            .thenReturn(WikipediaSearchResult.builder().total(4).pageId(45).build());
        when(customRepository.findPagesReviewed(any(WikipediaLanguage.class), any(CustomType.class))).thenReturn(
            Stream.of(12, 23, 34, 45).map(pageId -> PageKey.of(lang, pageId)).toList()
        );

        // Only call
        Optional<Review> review = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaPageRepository).findByContent(buildWikipediaSearchRequest(replacement), user.getAccessToken());
        verify(wikipediaPageRepository).findByContent(
            buildWikipediaSearchRequest(replacement, 3),
            user.getAccessToken()
        );
        verify(customRepository).findPagesReviewed(lang, customType);
        verify(wikipediaPageRepository, never()).findByKey(any(PageKey.class), any(AccessToken.class));
    }

    @Test
    void testSeveralResults() {
        // 4 Wikipedia results
        // The user will review with changes results 1, 3
        // The user will review with no changes the rest, i.e. 2, 4

        final String content = "A R";
        final Replacement customRep = Replacement.of(
            2,
            replacement,
            customType,
            List.of(Suggestion.ofNoComment(suggestion))
        );
        final Map<Integer, WikipediaPage> pages = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            pages.put(i, buildWikipediaPage(i, content));
        }

        // Mocks
        when(wikipediaPageRepository.findByContent(any(WikipediaSearchRequest.class), any(AccessToken.class)))
            .thenReturn(WikipediaSearchResult.builder().total(4).pageId(1).pageId(2).pageId(3).build()) // Call 1
            .thenReturn(WikipediaSearchResult.builder().total(2).pageId(2).pageId(4).build()) // Call 4
            .thenReturn(WikipediaSearchResult.builder().total(2).pageId(2).pageId(4).build()); // Call 5

        when(customRepository.findPagesReviewed(any(WikipediaLanguage.class), any(CustomType.class)))
            .thenReturn(List.of()) // Call 1
            .thenReturn(Stream.of(1, 2, 3).map(pageId -> PageKey.of(lang, pageId)).toList()) // Call 4
            .thenReturn(Stream.of(1, 2, 3, 4).map(pageId -> PageKey.of(lang, pageId)).toList()); // Call 5

        when(wikipediaPageRepository.findByKey(any(PageKey.class), any(AccessToken.class)))
            .thenReturn(Optional.of(pages.get(1))) // Call 1
            .thenReturn(Optional.of(pages.get(2))) // Call 2
            .thenReturn(Optional.of(pages.get(3))) // Call 3
            .thenReturn(Optional.of(pages.get(4))); // Call 4

        when(pageIndexApi.indexPage(any(WikipediaPage.class))).thenReturn(PageIndexResult.ofIndexed());

        when(
            customReplacementFindApi.findCustomReplacements(
                any(FinderPage.class),
                any(CustomReplacementFindRequest.class)
            )
        ).thenReturn(Stream.of(customRep).collect(Collectors.toCollection(TreeSet::new)));

        // We cannot use the same options object for all calls as it is mutable (and mutated)
        // Call 1
        Optional<Review> review = pageReviewCustomService.findRandomPageReview(options);
        assertFalse(review.isEmpty());
        review.ifPresent(r -> {
            assertEquals(1, r.getPage().getPageId());
            assertEquals(4, r.getNumPending());
        });
        // Cache: 2, 3

        // Call 2
        review = pageReviewCustomService.findRandomPageReview(options);
        assertFalse(review.isEmpty());
        review.ifPresent(r -> {
            assertEquals(2, r.getPage().getPageId());
            assertEquals(3, r.getNumPending());
        });
        // Cache: 3

        // Call 3
        review = pageReviewCustomService.findRandomPageReview(options);
        assertFalse(review.isEmpty());
        review.ifPresent(r -> {
            assertEquals(3, r.getPage().getPageId());
            assertEquals(2, r.getNumPending());
        });
        // Cache: empty

        // Call 4
        review = pageReviewCustomService.findRandomPageReview(options);
        assertFalse(review.isEmpty());
        review.ifPresent(r -> {
            assertEquals(4, r.getPage().getPageId());
            assertEquals(1, r.getNumPending());
        });
        // Cache: empty

        // Call 5: To start again after message of no more results
        review = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaPageRepository, times(2)).findByContent(
            buildWikipediaSearchRequest(replacement),
            user.getAccessToken()
        );
        verify(customRepository, times(2)).findPagesReviewed(lang, customType);
        verify(wikipediaPageRepository, times(4)).findByKey(any(PageKey.class), any(AccessToken.class));
    }

    @Test
    void testAllResultsWithoutReplacements() {
        // 4 Wikipedia results all without replacements

        final String content = "A R";
        final Map<Integer, WikipediaPage> pages = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            pages.put(i, buildWikipediaPage(i, content));
        }

        // Mocks
        when(wikipediaPageRepository.findByContent(any(WikipediaSearchRequest.class), any(AccessToken.class)))
            .thenReturn(WikipediaSearchResult.builder().total(4).pageId(1).pageId(2).pageId(3).build())
            .thenReturn(WikipediaSearchResult.builder().total(4).pageId(4).build())
            .thenReturn(WikipediaSearchResult.builder().total(4).build());

        when(customRepository.findPagesReviewed(any(WikipediaLanguage.class), any(CustomType.class))).thenReturn(
            List.of()
        );

        when(wikipediaPageRepository.findByKey(any(PageKey.class), any(AccessToken.class)))
            .thenReturn(Optional.of(pages.get(1))) // Call 1
            .thenReturn(Optional.of(pages.get(2))) // Call 2
            .thenReturn(Optional.of(pages.get(3))) // Call 3
            .thenReturn(Optional.of(pages.get(4))); // Call 4

        when(pageIndexApi.indexPage(any(WikipediaPage.class))).thenReturn(PageIndexResult.ofIndexed());

        when(
            customReplacementFindApi.findCustomReplacements(
                any(FinderPage.class),
                any(CustomReplacementFindRequest.class)
            )
        ).thenReturn(new TreeSet<>());

        // Only Call
        Optional<Review> review = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaPageRepository).findByContent(buildWikipediaSearchRequest(replacement), user.getAccessToken());
        verify(wikipediaPageRepository).findByContent(
            buildWikipediaSearchRequest(replacement, CACHE_SIZE),
            user.getAccessToken()
        );
        verify(wikipediaPageRepository).findByContent(
            buildWikipediaSearchRequest(replacement, 2 * CACHE_SIZE),
            user.getAccessToken()
        );
        verify(customRepository, times(2)).findPagesReviewed(lang, customType);
        verify(wikipediaPageRepository, times(4)).findByKey(any(PageKey.class), any(AccessToken.class));
    }

    @Test
    void testSameReplacementStandardAndCustom() {
        int id = 123;
        WikipediaPage page = buildWikipediaPage(id, "Y lucho.");

        String subtype = "lucho";
        String comment = "luchó";
        ReviewOptions options = ReviewOptions.ofCustom(user, subtype, true, comment);

        Suggestion suggestion = Suggestion.ofNoComment(comment);
        final Replacement replacement = Replacement.of(
            2,
            subtype,
            StandardType.of(ReplacementKind.SIMPLE, subtype),
            List.of(suggestion)
        );
        Collection<Replacement> replacements = List.of(replacement);

        final Replacement custom = Replacement.of(2, subtype, options.getCustomType(), List.of(suggestion));
        when(
            customReplacementFindApi.findCustomReplacements(
                FinderPage.of(page),
                options.getCustomReplacementFindRequest()
            )
        ).thenReturn(Stream.of(custom).collect(Collectors.toCollection(TreeSet::new)));

        Collection<Replacement> result = pageReviewCustomService.decorateReplacements(page, options, replacements);

        assertEquals(1, result.size());

        verify(customReplacementFindApi).findCustomReplacements(
            FinderPage.of(page),
            options.getCustomReplacementFindRequest()
        );
    }

    @Test
    void testCustomContainsStandard() {
        int id = 123;
        WikipediaPage page = buildWikipediaPage(id, "Un Seat Leon.");

        String subtype = "Seat Leon";
        String comment = "Seat León";
        ReviewOptions options = ReviewOptions.ofCustom(user, subtype, true, comment);

        final Replacement replacement = Replacement.of(
            8,
            "Leon",
            StandardType.of(ReplacementKind.SIMPLE, "leon"),
            List.of(Suggestion.ofNoComment("León"))
        );
        Collection<Replacement> replacements = List.of(replacement);

        final Replacement custom = Replacement.of(
            3,
            subtype,
            options.getCustomType(),
            List.of(Suggestion.ofNoComment(comment))
        );
        when(
            customReplacementFindApi.findCustomReplacements(
                FinderPage.of(page),
                options.getCustomReplacementFindRequest()
            )
        ).thenReturn(Stream.of(custom).collect(Collectors.toCollection(TreeSet::new)));

        Collection<Replacement> result = pageReviewCustomService.decorateReplacements(page, options, replacements);

        assertEquals(Set.of(custom), new HashSet<>(result));

        verify(customReplacementFindApi).findCustomReplacements(
            FinderPage.of(page),
            options.getCustomReplacementFindRequest()
        );
    }

    @Test
    void testStandardContainsCustom() {
        int id = 123;
        WikipediaPage page = buildWikipediaPage(id, "En Septiembre de 2020.");

        String subtype = "En Septiembre";
        String comment = "En septiembre";
        ReviewOptions options = ReviewOptions.ofCustom(user, subtype, true, comment);

        final Replacement replacement = Replacement.of(
            0,
            "En Septiembre de 2020",
            StandardType.DATE,
            List.of(Suggestion.ofNoComment("En septiembre de 2020"))
        );
        Collection<Replacement> replacements = List.of(replacement);

        final Replacement custom = Replacement.of(
            0,
            subtype,
            options.getCustomType(),
            List.of(Suggestion.ofNoComment("En septiembre"))
        );
        when(
            customReplacementFindApi.findCustomReplacements(
                FinderPage.of(page),
                options.getCustomReplacementFindRequest()
            )
        ).thenReturn(Stream.of(custom).collect(Collectors.toCollection(TreeSet::new)));

        Collection<Replacement> result = pageReviewCustomService.decorateReplacements(page, options, replacements);

        // As we prefer the standard replacement to the custom one,
        // there is no custom replacement to review.
        assertTrue(result.isEmpty());

        verify(customReplacementFindApi).findCustomReplacements(
            FinderPage.of(page),
            options.getCustomReplacementFindRequest()
        );
    }
}
