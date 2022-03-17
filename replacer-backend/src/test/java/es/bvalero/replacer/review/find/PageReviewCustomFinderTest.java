package es.bvalero.replacer.review.find;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.domain.WikipediaSearchResult;
import es.bvalero.replacer.page.findreplacement.PageReplacementFinder;
import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.page.index.PageIndexService;
import es.bvalero.replacer.page.index.PageIndexStatus;
import es.bvalero.replacer.repository.CustomModel;
import es.bvalero.replacer.repository.CustomRepository;
import es.bvalero.replacer.repository.PageIndexRepository;
import es.bvalero.replacer.repository.PageRepository;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageReviewCustomFinderTest {

    private static final int CACHE_SIZE = 3;
    private static final Collection<WikipediaNamespace> NAMESPACES = Collections.singleton(
        WikipediaNamespace.getDefault()
    );

    @Mock
    private PageIndexRepository pageIndexRepository;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private CustomRepository customRepository;

    @Mock
    private WikipediaPageRepository wikipediaPageRepository;

    @Mock
    private PageReplacementFinder pageReplacementFinder;

    @Mock
    private PageReviewSectionFinder pageReviewSectionFinder;

    @Mock
    private PageIndexService pageIndexService;

    @InjectMocks
    private PageReviewCustomFinder pageReviewCustomService;

    @BeforeEach
    public void setUp() {
        pageReviewCustomService = new PageReviewCustomFinder();
        pageReviewCustomService.setCacheSize(CACHE_SIZE);
        pageReviewCustomService.setIndexableNamespaces(
            NAMESPACES.stream().map(WikipediaNamespace::getValue).collect(Collectors.toUnmodifiableSet())
        );
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testNoResults() {
        final WikipediaLanguage lang = WikipediaLanguage.getDefault();
        final String replacement = "R";
        final String suggestion = "S";

        // No results in Wikipedia Search ==> Return an empty review

        when(
            wikipediaPageRepository.findByContent(
                any(WikipediaLanguage.class),
                anyCollection(),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()
            )
        )
            .thenReturn(WikipediaSearchResult.ofEmpty());

        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);

        assertTrue(review.isEmpty());

        verify(wikipediaPageRepository).findByContent(lang, NAMESPACES, replacement, true, 0, CACHE_SIZE);
        verify(customRepository, never()).findPageIdsReviewed(any(WikipediaLanguage.class), anyString(), anyBoolean());
        verify(customRepository, never()).addCustom(any(CustomModel.class));
    }

    @Test
    void testResultAlreadyReviewed() {
        final WikipediaLanguage lang = WikipediaLanguage.getDefault();
        final String replacement = "R";
        final String suggestion = "S";

        // Search in Wikipedia returns a result which is already reviewed in database
        // ==> Return an empty review

        final int pageId = 123;
        final WikipediaSearchResult searchResult = WikipediaSearchResult.builder().total(1).pageId(pageId).build();

        // Mocks
        when(
            wikipediaPageRepository.findByContent(
                any(WikipediaLanguage.class),
                anyCollection(),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()
            )
        )
            .thenReturn(searchResult);
        when(customRepository.findPageIdsReviewed(any(WikipediaLanguage.class), anyString(), anyBoolean()))
            .thenReturn(List.of(pageId));

        // Only one call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaPageRepository).findByContent(lang, NAMESPACES, replacement, true, 0, CACHE_SIZE);
        verify(customRepository).findPageIdsReviewed(lang, replacement, true);
        verify(customRepository, never()).addCustom(any(CustomModel.class));
    }

    @Test
    void testResultWithReview() {
        final WikipediaLanguage lang = WikipediaLanguage.getDefault();
        final String replacement = "R";
        final String suggestion = "S";

        // Search in Wikipedia returns a result which is not reviewed yet
        // ==> Return a review for that result
        // The user reviews the page so there are no more results to review
        // ==> Return an empty review

        final int pageId = 123;
        final String content = "A R";
        final Replacement customRep = Replacement
            .builder()
            .start(2)
            .type(ReplacementType.of(ReplacementKind.CUSTOM, "R"))
            .text("R")
            .suggestions(List.of(Suggestion.ofNoComment("Z")))
            .build();
        final WikipediaSearchResult searchResult = WikipediaSearchResult.builder().total(1).pageId(pageId).build();
        final WikipediaPageId wikipediaPageId = WikipediaPageId.of(lang, pageId);
        final WikipediaPage page = WikipediaPage
            .builder()
            .id(wikipediaPageId)
            .namespace(WikipediaNamespace.getDefault())
            .title("Title")
            .content(content)
            .lastUpdate(LocalDateTime.now())
            .build();

        // Mocks
        when(
            wikipediaPageRepository.findByContent(
                any(WikipediaLanguage.class),
                anyCollection(),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()
            )
        )
            .thenReturn(searchResult);
        when(customRepository.findPageIdsReviewed(any(WikipediaLanguage.class), anyString(), anyBoolean()))
            .thenReturn(Collections.emptyList());
        when(wikipediaPageRepository.findById(any(WikipediaPageId.class))).thenReturn(Optional.of(page));
        when(pageIndexService.indexPage(page))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_INDEXED, Collections.emptyList()));
        when(pageReplacementFinder.findCustomReplacements(any(WikipediaPage.class), any(PageReviewOptions.class)))
            .thenReturn(List.of(customRep));

        // First call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review1 = pageReviewCustomService.findRandomPageReview(options);
        assertFalse(review1.isEmpty());
        review1.ifPresent(r -> {
            assertEquals(lang, r.getPage().getId().getLang());
            assertEquals(pageId, r.getPage().getId().getPageId());
            assertEquals(content, r.getPage().getContent());
            assertEquals(1, r.getNumPending());
        });

        // Second call
        Optional<PageReview> review2 = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review2.isEmpty());

        // Verifications
        verify(wikipediaPageRepository).findByContent(lang, NAMESPACES, replacement, true, 0, CACHE_SIZE);
        verify(customRepository).findPageIdsReviewed(lang, replacement, true);
        verify(wikipediaPageRepository).findById(wikipediaPageId);
        verify(customRepository).addCustom(any(CustomModel.class));
    }

    @Test
    void testResultWithNoReplacements() {
        final WikipediaLanguage lang = WikipediaLanguage.getDefault();
        final String replacement = "R";
        final String suggestion = "S";

        // Search in Wikipedia returns a result which is not reviewed yet but the page has no replacements
        // ==> Return an empty review
        // We don't insert any fake replacement in database.
        // Custom replacements are not re-indexed, so we wouldn't detect future changes in the page.

        final int pageId = 123;
        final String content = "A R";
        final WikipediaSearchResult searchResult = WikipediaSearchResult.builder().total(1).pageId(pageId).build();
        final WikipediaPageId wikipediaPageId = WikipediaPageId.of(lang, pageId);
        final WikipediaPage page = WikipediaPage
            .builder()
            .id(wikipediaPageId)
            .namespace(WikipediaNamespace.getDefault())
            .title("Title")
            .content(content)
            .lastUpdate(LocalDateTime.now())
            .build();

        // Mocks
        when(
            wikipediaPageRepository.findByContent(
                any(WikipediaLanguage.class),
                anyCollection(),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()
            )
        )
            .thenReturn(searchResult);
        when(customRepository.findPageIdsReviewed(any(WikipediaLanguage.class), anyString(), anyBoolean()))
            .thenReturn(Collections.emptyList());
        when(wikipediaPageRepository.findById(any(WikipediaPageId.class))).thenReturn(Optional.of(page));
        when(pageIndexService.indexPage(page))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_INDEXED, Collections.emptyList()));
        when(pageReplacementFinder.findCustomReplacements(any(WikipediaPage.class), any(PageReviewOptions.class)))
            .thenReturn(Collections.emptyList());

        // Only call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaPageRepository).findByContent(lang, NAMESPACES, replacement, true, 0, CACHE_SIZE);
        verify(customRepository).findPageIdsReviewed(lang, replacement, true);
        verify(wikipediaPageRepository).findById(wikipediaPageId);
        verify(customRepository, never()).addCustom(any(CustomModel.class));
    }

    @Test
    void testTwoResultsFirstReviewed() {
        final WikipediaLanguage lang = WikipediaLanguage.getDefault();
        final String replacement = "R";
        final String suggestion = "S";

        // Search in Wikipedia returns two results with the first one reviewed in database
        // ==> Return a review for the second result

        final int pageId1 = 123;
        final int pageId2 = 456;
        final String content = "A R";
        final Replacement customRep = Replacement
            .builder()
            .start(2)
            .type(ReplacementType.of(ReplacementKind.CUSTOM, "R"))
            .text("R")
            .suggestions(List.of(Suggestion.ofNoComment("Z")))
            .build();
        final WikipediaSearchResult searchResult = WikipediaSearchResult
            .builder()
            .total(2)
            .pageId(pageId1)
            .pageId(pageId2)
            .build();
        final WikipediaPageId wikipediaPageId2 = WikipediaPageId.of(lang, pageId2);
        final WikipediaPage page = WikipediaPage
            .builder()
            .id(wikipediaPageId2)
            .title("Title")
            .namespace(WikipediaNamespace.getDefault())
            .content(content)
            .lastUpdate(LocalDateTime.now())
            .build();

        // Mocks
        when(
            wikipediaPageRepository.findByContent(
                any(WikipediaLanguage.class),
                anyCollection(),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()
            )
        )
            .thenReturn(searchResult);
        when(customRepository.findPageIdsReviewed(any(WikipediaLanguage.class), anyString(), anyBoolean()))
            .thenReturn(List.of(pageId1));
        when(wikipediaPageRepository.findById(any(WikipediaPageId.class))).thenReturn(Optional.of(page));
        when(pageIndexService.indexPage(page))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_INDEXED, Collections.emptyList()));
        when(pageReplacementFinder.findCustomReplacements(any(WikipediaPage.class), any(PageReviewOptions.class)))
            .thenReturn(List.of(customRep));

        // Only call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        assertFalse(review.isEmpty());
        review.ifPresent(r -> {
            assertEquals(lang, r.getPage().getId().getLang());
            assertEquals(pageId2, r.getPage().getId().getPageId());
            assertEquals(content, r.getPage().getContent());
            assertEquals(1, r.getNumPending());
        });

        // Verifications
        verify(wikipediaPageRepository).findByContent(lang, NAMESPACES, replacement, true, 0, CACHE_SIZE);
        verify(customRepository).findPageIdsReviewed(lang, replacement, true);
        verify(wikipediaPageRepository).findById(wikipediaPageId2);
        verify(customRepository).addCustom(any(CustomModel.class));
    }

    @Test
    void testSeveralResultsAllReviewed() {
        final WikipediaLanguage lang = WikipediaLanguage.getDefault();
        final String replacement = "R";
        final String suggestion = "S";

        // Search in Wikipedia returns 4 results (pagination = 3) all of them reviewed in database
        // so we perform two calls to Wikipedia search
        // ==> Return an empty review

        // Mocks
        when(
            wikipediaPageRepository.findByContent(
                any(WikipediaLanguage.class),
                anyCollection(),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()
            )
        )
            .thenReturn(WikipediaSearchResult.builder().total(4).pageId(12).pageId(23).pageId(34).build())
            .thenReturn(WikipediaSearchResult.builder().total(4).pageId(45).build());
        when(customRepository.findPageIdsReviewed(any(WikipediaLanguage.class), anyString(), anyBoolean()))
            .thenReturn(List.of(12, 23, 34, 45));

        // Only call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaPageRepository).findByContent(lang, NAMESPACES, replacement, true, 0, CACHE_SIZE);
        verify(wikipediaPageRepository).findByContent(lang, NAMESPACES, replacement, true, 3, CACHE_SIZE);
        verify(customRepository).findPageIdsReviewed(lang, replacement, true);
        verify(wikipediaPageRepository, never()).findById(any(WikipediaPageId.class));
        verify(customRepository, never()).addCustom(any(CustomModel.class));
    }

    @Test
    void testSeveralResults() {
        final WikipediaLanguage lang = WikipediaLanguage.getDefault();
        final String replacement = "R";
        final String suggestion = "S";

        // 4 Wikipedia results
        // The user will review with changes results 1, 3
        // The user will review with no changes the rest, i.e. 2, 4

        final String content = "A R";
        final Replacement customRep = Replacement
            .builder()
            .start(2)
            .type(ReplacementType.of(ReplacementKind.CUSTOM, "R"))
            .text("R")
            .suggestions(List.of(Suggestion.ofNoComment("Z")))
            .build();
        final Map<Integer, WikipediaPage> pages = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            pages.put(
                i,
                WikipediaPage
                    .builder()
                    .id(WikipediaPageId.of(lang, i))
                    .namespace(WikipediaNamespace.getDefault())
                    .title("Title")
                    .content(content)
                    .lastUpdate(LocalDateTime.now())
                    .build()
            );
        }

        // Mocks
        when(
            wikipediaPageRepository.findByContent(
                any(WikipediaLanguage.class),
                anyCollection(),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()
            )
        )
            .thenReturn(WikipediaSearchResult.builder().total(4).pageId(1).pageId(2).pageId(3).build()) // Call 1
            .thenReturn(WikipediaSearchResult.builder().total(2).pageId(2).pageId(4).build()) // Call 4
            .thenReturn(WikipediaSearchResult.builder().total(2).pageId(2).pageId(4).build()); // Call 5

        when(customRepository.findPageIdsReviewed(any(WikipediaLanguage.class), anyString(), anyBoolean()))
            .thenReturn(Collections.emptyList()) // Call 1
            .thenReturn(List.of(1, 2, 3)) // Call 4
            .thenReturn(List.of(1, 2, 3, 4)); // Call 5

        when(wikipediaPageRepository.findById(any(WikipediaPageId.class)))
            .thenReturn(Optional.of(pages.get(1))) // Call 1
            .thenReturn(Optional.of(pages.get(2))) // Call 2
            .thenReturn(Optional.of(pages.get(3))) // Call 3
            .thenReturn(Optional.of(pages.get(4))); // Call 4

        when(pageIndexService.indexPage(any(WikipediaPage.class)))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_INDEXED, Collections.emptyList()));

        when(pageReplacementFinder.findCustomReplacements(any(WikipediaPage.class), any(PageReviewOptions.class)))
            .thenReturn(List.of(customRep));

        // We cannot use the same options object for all calls as it is mutable (and mutated)
        // Call 1
        PageReviewOptions options1 = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options1);
        assertFalse(review.isEmpty());
        review.ifPresent(r -> {
            assertEquals(1, r.getPage().getId().getPageId());
            assertEquals(4, r.getNumPending());
        });
        // Cache: 2, 3

        // Call 2
        PageReviewOptions options2 = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        review = pageReviewCustomService.findRandomPageReview(options2);
        assertFalse(review.isEmpty());
        review.ifPresent(r -> {
            assertEquals(2, r.getPage().getId().getPageId());
            assertEquals(3, r.getNumPending());
        });
        // Cache: 3

        // Call 3
        PageReviewOptions options3 = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        review = pageReviewCustomService.findRandomPageReview(options3);
        assertFalse(review.isEmpty());
        review.ifPresent(r -> {
            assertEquals(3, r.getPage().getId().getPageId());
            assertEquals(2, r.getNumPending());
        });
        // Cache: empty

        // Call 4
        PageReviewOptions options4 = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        review = pageReviewCustomService.findRandomPageReview(options4);
        assertFalse(review.isEmpty());
        review.ifPresent(r -> {
            assertEquals(4, r.getPage().getId().getPageId());
            assertEquals(1, r.getNumPending());
        });
        // Cache: empty

        // Call 5: To start again after message of no more results
        PageReviewOptions options5 = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        review = pageReviewCustomService.findRandomPageReview(options5);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaPageRepository, times(2)).findByContent(lang, NAMESPACES, replacement, true, 0, CACHE_SIZE);
        verify(customRepository, times(2)).findPageIdsReviewed(lang, replacement, true);
        verify(wikipediaPageRepository, times(4)).findById(any(WikipediaPageId.class));
        verify(customRepository, times(4)).addCustom(any(CustomModel.class));
    }

    @Test
    void testAllResultsWithoutReplacements() {
        final WikipediaLanguage lang = WikipediaLanguage.getDefault();
        final String replacement = "R";
        final String suggestion = "S";

        // 4 Wikipedia results all without replacements

        final String content = "A R";
        final Map<Integer, WikipediaPage> pages = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            pages.put(
                i,
                WikipediaPage
                    .builder()
                    .id(WikipediaPageId.of(lang, i))
                    .namespace(WikipediaNamespace.getDefault())
                    .title("Title")
                    .content(content)
                    .lastUpdate(LocalDateTime.now())
                    .build()
            );
        }

        // Mocks
        when(
            wikipediaPageRepository.findByContent(
                any(WikipediaLanguage.class),
                anyCollection(),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()
            )
        )
            .thenReturn(WikipediaSearchResult.builder().total(4).pageId(1).pageId(2).pageId(3).build())
            .thenReturn(WikipediaSearchResult.builder().total(4).pageId(4).build())
            .thenReturn(WikipediaSearchResult.builder().total(4).build());

        when(customRepository.findPageIdsReviewed(any(WikipediaLanguage.class), anyString(), anyBoolean()))
            .thenReturn(Collections.emptyList());

        when(wikipediaPageRepository.findById(any(WikipediaPageId.class)))
            .thenReturn(Optional.of(pages.get(1))) // Call 1
            .thenReturn(Optional.of(pages.get(2))) // Call 2
            .thenReturn(Optional.of(pages.get(3))) // Call 3
            .thenReturn(Optional.of(pages.get(4))); // Call 4

        when(pageIndexService.indexPage(any(WikipediaPage.class)))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_INDEXED, Collections.emptyList()));

        when(pageReplacementFinder.findCustomReplacements(any(WikipediaPage.class), any(PageReviewOptions.class)))
            .thenReturn(Collections.emptyList());

        // Only Call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaPageRepository).findByContent(lang, NAMESPACES, replacement, true, 0, CACHE_SIZE);
        verify(wikipediaPageRepository).findByContent(lang, NAMESPACES, replacement, true, CACHE_SIZE, CACHE_SIZE);
        verify(wikipediaPageRepository).findByContent(lang, NAMESPACES, replacement, true, 2 * CACHE_SIZE, CACHE_SIZE);
        verify(customRepository, times(2)).findPageIdsReviewed(lang, replacement, true);
        verify(wikipediaPageRepository, times(4)).findById(any(WikipediaPageId.class));
        verify(customRepository, never()).addCustom(any(CustomModel.class));
    }

    @Test
    void testSameReplacementStandardAndCustom() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        int id = 123;
        WikipediaPageId pageId = WikipediaPageId.of(lang, id);
        WikipediaPage page = WikipediaPage
            .builder()
            .id(pageId)
            .namespace(WikipediaNamespace.getDefault())
            .title("T")
            .content("Y lucho.")
            .lastUpdate(LocalDateTime.now())
            .build();

        PageReviewOptions options = PageReviewOptions.ofCustom(lang, "lucho", "luchó", true);

        Suggestion suggestion = Suggestion.ofNoComment("luchó");
        Replacement replacement = Replacement
            .builder()
            .start(2)
            .text("lucho")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "lucho"))
            .suggestions(List.of(suggestion))
            .build();
        Collection<Replacement> replacements = List.of(replacement);

        Replacement custom = Replacement
            .builder()
            .start(2)
            .text("lucho")
            .type(ReplacementType.of(ReplacementKind.CUSTOM, "lucho"))
            .suggestions(List.of(suggestion))
            .build();
        when(pageReplacementFinder.findCustomReplacements(page, options)).thenReturn(List.of(custom));

        Collection<Replacement> result = pageReviewCustomService.decorateReplacements(page, options, replacements);

        assertEquals(1, result.size());

        verify(pageReplacementFinder).findCustomReplacements(page, options);
    }

    @Test
    void testCustomContainsStandard() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        int id = 123;
        WikipediaPageId pageId = WikipediaPageId.of(lang, id);
        WikipediaPage page = WikipediaPage
            .builder()
            .id(pageId)
            .namespace(WikipediaNamespace.getDefault())
            .title("T")
            .content("Un Seat Leon.")
            .lastUpdate(LocalDateTime.now())
            .build();

        PageReviewOptions options = PageReviewOptions.ofCustom(lang, "Seat Leon", "Seat León", true);

        Replacement replacement = Replacement
            .builder()
            .start(8)
            .text("Leon")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "leon"))
            .suggestions(List.of(Suggestion.ofNoComment("León")))
            .build();
        Collection<Replacement> replacements = List.of(replacement);

        Replacement custom = Replacement
            .builder()
            .start(3)
            .text("Seat Leon")
            .type(ReplacementType.of(ReplacementKind.CUSTOM, "Seat Leon"))
            .suggestions(List.of(Suggestion.ofNoComment("Seat León")))
            .build();
        when(pageReplacementFinder.findCustomReplacements(page, options)).thenReturn(List.of(custom));

        Collection<Replacement> result = pageReviewCustomService.decorateReplacements(page, options, replacements);

        assertEquals(Set.of(custom), new HashSet<>(result));

        verify(pageReplacementFinder).findCustomReplacements(page, options);
    }

    @Test
    void testStandardContainsCustom() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        int id = 123;
        WikipediaPageId pageId = WikipediaPageId.of(lang, id);
        WikipediaPage page = WikipediaPage
            .builder()
            .id(pageId)
            .namespace(WikipediaNamespace.getDefault())
            .title("T")
            .content("En Septiembre de 2020.")
            .lastUpdate(LocalDateTime.now())
            .build();

        PageReviewOptions options = PageReviewOptions.ofCustom(lang, "En Septiembre", "En septiembre", true);

        Replacement replacement = Replacement
            .builder()
            .start(0)
            .text("En Septiembre de 2020")
            .type(ReplacementType.of(ReplacementKind.DATE, "Mes en mayúscula"))
            .suggestions(List.of(Suggestion.ofNoComment("En septiembre de 2020")))
            .build();
        Collection<Replacement> replacements = List.of(replacement);

        Replacement custom = Replacement
            .builder()
            .start(0)
            .text("En Septiembre")
            .type(ReplacementType.of(ReplacementKind.CUSTOM, "En Septiembre"))
            .suggestions(List.of(Suggestion.ofNoComment("En septiembre")))
            .build();
        when(pageReplacementFinder.findCustomReplacements(page, options)).thenReturn(List.of(custom));

        Collection<Replacement> result = pageReviewCustomService.decorateReplacements(page, options, replacements);

        assertEquals(Set.of(replacement), new HashSet<>(result));

        verify(pageReplacementFinder).findCustomReplacements(page, options);
    }
}
