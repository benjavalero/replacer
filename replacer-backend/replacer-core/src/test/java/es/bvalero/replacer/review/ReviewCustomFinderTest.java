package es.bvalero.replacer.review;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.CustomReplacementFindService;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.index.PageIndexResult;
import es.bvalero.replacer.index.PageIndexService;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.replacement.CustomReplacementService;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.user.UserRightsService;
import es.bvalero.replacer.wikipedia.*;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReviewCustomFinderTest {

    private static final int CACHE_SIZE = 3;
    private static final Collection<WikipediaNamespace> NAMESPACES = Collections.singleton(
        WikipediaNamespace.getDefault()
    );
    private static final User user = User.buildTestUser();
    private static final WikipediaLanguage lang = user.getId().getLang();

    private static final String replacement = "R";
    private static final String suggestion = "S";
    private static final CustomType customType = CustomType.of(replacement, true, suggestion);

    @Mock
    private CustomReplacementService customReplacementService;

    @Mock
    private WikipediaPageRepository wikipediaPageRepository;

    @Mock
    private CustomReplacementFindService customReplacementFindService;

    @Mock
    private ReviewSectionFinder reviewSectionFinder;

    @Mock
    private PageIndexService pageIndexService;

    @Mock
    private UserRightsService userRightsService;

    @InjectMocks
    private ReviewCustomFinder pageReviewCustomService;

    @BeforeEach
    public void setUp() {
        pageReviewCustomService = new ReviewCustomFinder();
        pageReviewCustomService.setCacheSize(CACHE_SIZE);
        pageReviewCustomService.setIndexableNamespaces(
            NAMESPACES.stream().map(WikipediaNamespace::getValue).collect(Collectors.toUnmodifiableSet())
        );
        MockitoAnnotations.openMocks(this);
    }

    private WikipediaPage buildWikipediaPage(int pageId, String content) {
        return WikipediaPage
            .builder()
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
        return WikipediaSearchRequest
            .builder()
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

        when(wikipediaPageRepository.findByContent(any(WikipediaSearchRequest.class)))
            .thenReturn(WikipediaSearchResult.ofEmpty());

        ReviewOptions options = ReviewOptions.ofCustom(user, customType);
        Optional<Review> review = pageReviewCustomService.findRandomPageReview(options);

        assertTrue(review.isEmpty());

        verify(wikipediaPageRepository).findByContent(buildWikipediaSearchRequest(replacement));
        verify(customReplacementService, never())
            .findPagesReviewed(any(WikipediaLanguage.class), any(CustomType.class));
    }

    @Test
    void testResultAlreadyReviewed() {
        // Search in Wikipedia returns a result which is already reviewed in database
        // ==> Return an empty review

        final int pageId = 123;
        final WikipediaSearchResult searchResult = WikipediaSearchResult.builder().total(1).pageId(pageId).build();

        // Mocks
        when(wikipediaPageRepository.findByContent(any(WikipediaSearchRequest.class))).thenReturn(searchResult);
        when(customReplacementService.findPagesReviewed(any(WikipediaLanguage.class), any(CustomType.class)))
            .thenReturn(List.of(pageId));

        // Only one call
        ReviewOptions options = ReviewOptions.ofCustom(user, customType);
        Optional<Review> review = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaPageRepository).findByContent(buildWikipediaSearchRequest(replacement));
        verify(customReplacementService).findPagesReviewed(lang, customType);
    }

    @Test
    void testResultWithReview() {
        // Search in Wikipedia returns a result which is not reviewed yet
        // ==> Return a review for that result
        // The user reviews the page so there are no more results to review
        // ==> Return an empty review

        final int pageId = 123;
        final String content = "A R";
        final Replacement customRep = Replacement
            .builder()
            .start(2)
            .type(customType)
            .text(replacement)
            .suggestions(List.of(Suggestion.ofNoComment(suggestion)))
            .build();
        final WikipediaSearchResult searchResult = WikipediaSearchResult.builder().total(1).pageId(pageId).build();
        final PageKey pageKey = PageKey.of(lang, pageId);
        final WikipediaPage page = buildWikipediaPage(pageId, content);

        // Mocks
        when(wikipediaPageRepository.findByContent(any(WikipediaSearchRequest.class))).thenReturn(searchResult);
        when(customReplacementService.findPagesReviewed(any(WikipediaLanguage.class), any(CustomType.class)))
            .thenReturn(Collections.emptyList());
        when(wikipediaPageRepository.findByKey(any(PageKey.class))).thenReturn(Optional.of(page));
        when(pageIndexService.indexPage(page)).thenReturn(PageIndexResult.ofIndexed());
        when(customReplacementFindService.findCustomReplacements(any(WikipediaPage.class), any(CustomType.class)))
            .thenReturn(List.of(customRep));

        // First call
        ReviewOptions options = ReviewOptions.ofCustom(user, customType);
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
        verify(wikipediaPageRepository).findByContent(buildWikipediaSearchRequest(replacement));
        verify(customReplacementService).findPagesReviewed(lang, customType);
        verify(wikipediaPageRepository).findByKey(pageKey);
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
        when(wikipediaPageRepository.findByContent(any(WikipediaSearchRequest.class))).thenReturn(searchResult);
        when(customReplacementService.findPagesReviewed(any(WikipediaLanguage.class), any(CustomType.class)))
            .thenReturn(Collections.emptyList());
        when(wikipediaPageRepository.findByKey(any(PageKey.class))).thenReturn(Optional.of(page));
        when(pageIndexService.indexPage(page)).thenReturn(PageIndexResult.ofIndexed());
        when(customReplacementFindService.findCustomReplacements(any(WikipediaPage.class), any(CustomType.class)))
            .thenReturn(Collections.emptyList());

        // Only call
        ReviewOptions options = ReviewOptions.ofCustom(user, customType);
        Optional<Review> review = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaPageRepository).findByContent(buildWikipediaSearchRequest(replacement));
        verify(customReplacementService).findPagesReviewed(lang, customType);
        verify(wikipediaPageRepository).findByKey(pageKey);
    }

    @Test
    void testTwoResultsFirstReviewed() {
        // Search in Wikipedia returns two results with the first one reviewed in database
        // ==> Return a review for the second result

        final int pageId1 = 123;
        final int pageId2 = 456;
        final String content = "A R";
        final Replacement customRep = Replacement
            .builder()
            .start(2)
            .type(customType)
            .text(replacement)
            .suggestions(List.of(Suggestion.ofNoComment(suggestion)))
            .build();
        final WikipediaSearchResult searchResult = WikipediaSearchResult
            .builder()
            .total(2)
            .pageId(pageId1)
            .pageId(pageId2)
            .build();
        final PageKey pageKey2 = PageKey.of(lang, pageId2);
        final WikipediaPage page = buildWikipediaPage(pageId2, content);

        // Mocks
        when(wikipediaPageRepository.findByContent(any(WikipediaSearchRequest.class))).thenReturn(searchResult);
        when(customReplacementService.findPagesReviewed(any(WikipediaLanguage.class), any(CustomType.class)))
            .thenReturn(List.of(pageId1));
        when(wikipediaPageRepository.findByKey(any(PageKey.class))).thenReturn(Optional.of(page));
        when(pageIndexService.indexPage(page)).thenReturn(PageIndexResult.ofIndexed());
        when(customReplacementFindService.findCustomReplacements(any(WikipediaPage.class), any(CustomType.class)))
            .thenReturn(List.of(customRep));

        // Only call
        ReviewOptions options = ReviewOptions.ofCustom(user, customType);
        Optional<Review> review = pageReviewCustomService.findRandomPageReview(options);
        assertFalse(review.isEmpty());
        review.ifPresent(r -> {
            assertEquals(lang, r.getPage().getPageKey().getLang());
            assertEquals(pageId2, r.getPage().getPageId());
            assertEquals(content, r.getPage().getContent());
            assertEquals(1, r.getNumPending());
        });

        // Verifications
        verify(wikipediaPageRepository).findByContent(buildWikipediaSearchRequest(replacement));
        verify(customReplacementService).findPagesReviewed(lang, customType);
        verify(wikipediaPageRepository).findByKey(pageKey2);
    }

    @Test
    void testSeveralResultsAllReviewed() {
        // Search in Wikipedia returns 4 results (pagination = 3) all of them reviewed in database
        // so we perform two calls to Wikipedia search
        // ==> Return an empty review

        // Mocks
        when(wikipediaPageRepository.findByContent(any(WikipediaSearchRequest.class)))
            .thenReturn(WikipediaSearchResult.builder().total(4).pageId(12).pageId(23).pageId(34).build())
            .thenReturn(WikipediaSearchResult.builder().total(4).pageId(45).build());
        when(customReplacementService.findPagesReviewed(any(WikipediaLanguage.class), any(CustomType.class)))
            .thenReturn(List.of(12, 23, 34, 45));

        // Only call
        ReviewOptions options = ReviewOptions.ofCustom(user, customType);
        Optional<Review> review = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaPageRepository).findByContent(buildWikipediaSearchRequest(replacement));
        verify(wikipediaPageRepository).findByContent(buildWikipediaSearchRequest(replacement, 3));
        verify(customReplacementService).findPagesReviewed(lang, customType);
        verify(wikipediaPageRepository, never()).findByKey(any(PageKey.class));
    }

    @Test
    void testSeveralResults() {
        // 4 Wikipedia results
        // The user will review with changes results 1, 3
        // The user will review with no changes the rest, i.e. 2, 4

        final String content = "A R";
        final Replacement customRep = Replacement
            .builder()
            .start(2)
            .type(customType)
            .text(replacement)
            .suggestions(List.of(Suggestion.ofNoComment(suggestion)))
            .build();
        final Map<Integer, WikipediaPage> pages = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            pages.put(i, buildWikipediaPage(i, content));
        }

        // Mocks
        when(wikipediaPageRepository.findByContent(any(WikipediaSearchRequest.class)))
            .thenReturn(WikipediaSearchResult.builder().total(4).pageId(1).pageId(2).pageId(3).build()) // Call 1
            .thenReturn(WikipediaSearchResult.builder().total(2).pageId(2).pageId(4).build()) // Call 4
            .thenReturn(WikipediaSearchResult.builder().total(2).pageId(2).pageId(4).build()); // Call 5

        when(customReplacementService.findPagesReviewed(any(WikipediaLanguage.class), any(CustomType.class)))
            .thenReturn(Collections.emptyList()) // Call 1
            .thenReturn(List.of(1, 2, 3)) // Call 4
            .thenReturn(List.of(1, 2, 3, 4)); // Call 5

        when(wikipediaPageRepository.findByKey(any(PageKey.class)))
            .thenReturn(Optional.of(pages.get(1))) // Call 1
            .thenReturn(Optional.of(pages.get(2))) // Call 2
            .thenReturn(Optional.of(pages.get(3))) // Call 3
            .thenReturn(Optional.of(pages.get(4))); // Call 4

        when(pageIndexService.indexPage(any(WikipediaPage.class))).thenReturn(PageIndexResult.ofIndexed());

        when(customReplacementFindService.findCustomReplacements(any(WikipediaPage.class), any(CustomType.class)))
            .thenReturn(List.of(customRep));

        // We cannot use the same options object for all calls as it is mutable (and mutated)
        // Call 1
        ReviewOptions options1 = ReviewOptions.ofCustom(user, customType);
        Optional<Review> review = pageReviewCustomService.findRandomPageReview(options1);
        assertFalse(review.isEmpty());
        review.ifPresent(r -> {
            assertEquals(1, r.getPage().getPageId());
            assertEquals(4, r.getNumPending());
        });
        // Cache: 2, 3

        // Call 2
        ReviewOptions options2 = ReviewOptions.ofCustom(user, customType);
        review = pageReviewCustomService.findRandomPageReview(options2);
        assertFalse(review.isEmpty());
        review.ifPresent(r -> {
            assertEquals(2, r.getPage().getPageId());
            assertEquals(3, r.getNumPending());
        });
        // Cache: 3

        // Call 3
        ReviewOptions options3 = ReviewOptions.ofCustom(user, customType);
        review = pageReviewCustomService.findRandomPageReview(options3);
        assertFalse(review.isEmpty());
        review.ifPresent(r -> {
            assertEquals(3, r.getPage().getPageId());
            assertEquals(2, r.getNumPending());
        });
        // Cache: empty

        // Call 4
        ReviewOptions options4 = ReviewOptions.ofCustom(user, customType);
        review = pageReviewCustomService.findRandomPageReview(options4);
        assertFalse(review.isEmpty());
        review.ifPresent(r -> {
            assertEquals(4, r.getPage().getPageId());
            assertEquals(1, r.getNumPending());
        });
        // Cache: empty

        // Call 5: To start again after message of no more results
        ReviewOptions options5 = ReviewOptions.ofCustom(user, customType);
        review = pageReviewCustomService.findRandomPageReview(options5);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaPageRepository, times(2)).findByContent(buildWikipediaSearchRequest(replacement));
        verify(customReplacementService, times(2)).findPagesReviewed(lang, customType);
        verify(wikipediaPageRepository, times(4)).findByKey(any(PageKey.class));
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
        when(wikipediaPageRepository.findByContent(any(WikipediaSearchRequest.class)))
            .thenReturn(WikipediaSearchResult.builder().total(4).pageId(1).pageId(2).pageId(3).build())
            .thenReturn(WikipediaSearchResult.builder().total(4).pageId(4).build())
            .thenReturn(WikipediaSearchResult.builder().total(4).build());

        when(customReplacementService.findPagesReviewed(any(WikipediaLanguage.class), any(CustomType.class)))
            .thenReturn(Collections.emptyList());

        when(wikipediaPageRepository.findByKey(any(PageKey.class)))
            .thenReturn(Optional.of(pages.get(1))) // Call 1
            .thenReturn(Optional.of(pages.get(2))) // Call 2
            .thenReturn(Optional.of(pages.get(3))) // Call 3
            .thenReturn(Optional.of(pages.get(4))); // Call 4

        when(pageIndexService.indexPage(any(WikipediaPage.class))).thenReturn(PageIndexResult.ofIndexed());

        when(customReplacementFindService.findCustomReplacements(any(WikipediaPage.class), any(CustomType.class)))
            .thenReturn(Collections.emptyList());

        // Only Call
        ReviewOptions options = ReviewOptions.ofCustom(user, customType);
        Optional<Review> review = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaPageRepository).findByContent(buildWikipediaSearchRequest(replacement));
        verify(wikipediaPageRepository).findByContent(buildWikipediaSearchRequest(replacement, CACHE_SIZE));
        verify(wikipediaPageRepository).findByContent(buildWikipediaSearchRequest(replacement, 2 * CACHE_SIZE));
        verify(customReplacementService, times(2)).findPagesReviewed(lang, customType);
        verify(wikipediaPageRepository, times(4)).findByKey(any(PageKey.class));
    }

    @Test
    void testSameReplacementStandardAndCustom() {
        int id = 123;
        WikipediaPage page = buildWikipediaPage(id, "Y lucho.");

        String subtype = "lucho";
        String comment = "luchó";
        CustomType customType = CustomType.of(subtype, true, comment);
        ReviewOptions options = ReviewOptions.ofCustom(user, customType);

        Suggestion suggestion = Suggestion.ofNoComment(comment);
        Replacement replacement = Replacement
            .builder()
            .start(2)
            .text(subtype)
            .type(StandardType.of(ReplacementKind.SIMPLE, subtype))
            .suggestions(List.of(suggestion))
            .build();
        Collection<Replacement> replacements = List.of(replacement);

        Replacement custom = Replacement
            .builder()
            .start(2)
            .text(subtype)
            .type(customType)
            .suggestions(List.of(suggestion))
            .build();
        when(customReplacementFindService.findCustomReplacements(page, customType)).thenReturn(List.of(custom));

        Collection<Replacement> result = pageReviewCustomService.decorateReplacements(page, options, replacements);

        assertEquals(1, result.size());

        verify(customReplacementFindService).findCustomReplacements(page, customType);
    }

    @Test
    void testCustomContainsStandard() {
        int id = 123;
        WikipediaPage page = buildWikipediaPage(id, "Un Seat Leon.");

        CustomType customType = CustomType.of("Seat Leon", true, "Seat León");
        ReviewOptions options = ReviewOptions.ofCustom(user, customType);

        Replacement replacement = Replacement
            .builder()
            .start(8)
            .text("Leon")
            .type(StandardType.of(ReplacementKind.SIMPLE, "leon"))
            .suggestions(List.of(Suggestion.ofNoComment("León")))
            .build();
        Collection<Replacement> replacements = List.of(replacement);

        Replacement custom = Replacement
            .builder()
            .start(3)
            .text(customType.getSubtype())
            .type(options.getType())
            .suggestions(List.of(Suggestion.ofNoComment(customType.getSuggestion())))
            .build();
        when(customReplacementFindService.findCustomReplacements(page, customType)).thenReturn(List.of(custom));

        Collection<Replacement> result = pageReviewCustomService.decorateReplacements(page, options, replacements);

        assertEquals(Set.of(custom), new HashSet<>(result));

        verify(customReplacementFindService).findCustomReplacements(page, customType);
    }

    @Test
    void testStandardContainsCustom() {
        int id = 123;
        WikipediaPage page = buildWikipediaPage(id, "En Septiembre de 2020.");

        CustomType customType = CustomType.of("En Septiembre", true, "En septiembre");
        ReviewOptions options = ReviewOptions.ofCustom(user, customType);

        Replacement replacement = Replacement
            .builder()
            .start(0)
            .text("En Septiembre de 2020")
            .type(StandardType.DATE)
            .suggestions(List.of(Suggestion.ofNoComment("En septiembre de 2020")))
            .build();
        Collection<Replacement> replacements = List.of(replacement);

        Replacement custom = Replacement
            .builder()
            .start(0)
            .text(customType.getSubtype())
            .type(customType)
            .suggestions(List.of(Suggestion.ofNoComment(customType.getSuggestion())))
            .build();
        when(customReplacementFindService.findCustomReplacements(page, customType)).thenReturn(List.of(custom));

        Collection<Replacement> result = pageReviewCustomService.decorateReplacements(page, options, replacements);

        // As we prefer the standard replacement to the custom one,
        // there is no custom replacement to review.
        assertTrue(result.isEmpty());

        verify(customReplacementFindService).findCustomReplacements(page, customType);
    }
}
