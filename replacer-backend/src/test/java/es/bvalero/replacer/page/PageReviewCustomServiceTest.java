package es.bvalero.replacer.page;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementSuggestion;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import es.bvalero.replacer.finder.replacement.custom.CustomOptions;
import es.bvalero.replacer.finder.replacement.custom.CustomReplacementFinderService;
import es.bvalero.replacer.page.review.PageReviewSectionFinder;
import es.bvalero.replacer.replacement.CustomEntity;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaSearchResult;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageReviewCustomServiceTest {

    private static final int CACHE_SIZE = 3;

    @Mock
    private ReplacementService replacementService;

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private CustomReplacementFinderService customReplacementFinderService;

    @Mock
    private PageReviewSectionFinder pageReviewSectionFinder;

    @InjectMocks
    private PageReviewCustomService pageReviewCustomService;

    @BeforeEach
    public void setUp() {
        pageReviewCustomService = new PageReviewCustomService();
        pageReviewCustomService.setCacheSize(CACHE_SIZE);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testNoResults() throws ReplacerException {
        final WikipediaLanguage lang = WikipediaLanguage.getDefault();
        final String replacement = "R";
        final String suggestion = "S";

        // No results in Wikipedia Search ==> Return an empty review

        when(wikipediaService.searchByText(any(WikipediaLanguage.class), anyString(), anyBoolean(), anyInt(), anyInt()))
            .thenReturn(WikipediaSearchResult.ofEmpty());

        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);

        assertTrue(review.isEmpty());

        verify(wikipediaService).searchByText(lang, replacement, true, 0, CACHE_SIZE);
        verify(replacementService, never())
            .findPageIdsReviewedByReplacement(any(WikipediaLanguage.class), anyString(), anyBoolean());
        verify(replacementService, never()).insert(any(CustomEntity.class));
    }

    @Test
    void testResultAlreadyReviewed() throws ReplacerException {
        final WikipediaLanguage lang = WikipediaLanguage.getDefault();
        final String replacement = "R";
        final String suggestion = "S";

        // Search in Wikipedia returns a result which is already reviewed in database
        // ==> Return an empty review

        final int pageId = 123;
        final WikipediaSearchResult searchResult = WikipediaSearchResult.of(1, List.of(pageId));

        // Mocks
        when(wikipediaService.searchByText(any(WikipediaLanguage.class), anyString(), anyBoolean(), anyInt(), anyInt()))
            .thenReturn(searchResult);
        when(
            replacementService.findPageIdsReviewedByReplacement(any(WikipediaLanguage.class), anyString(), anyBoolean())
        )
            .thenReturn(List.of(pageId));

        // Only one call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaService).searchByText(lang, replacement, true, 0, CACHE_SIZE);
        verify(replacementService).findPageIdsReviewedByReplacement(lang, replacement, true);
        verify(replacementService, never()).insert(any(CustomEntity.class));
    }

    @Test
    void testResultWithReview() throws ReplacerException {
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
            .type(ReplacementType.CUSTOM)
            .subtype("R")
            .text("R")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("Z")))
            .build();
        final WikipediaSearchResult searchResult = WikipediaSearchResult.of(1, List.of(pageId));
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
        when(wikipediaService.searchByText(any(WikipediaLanguage.class), anyString(), anyBoolean(), anyInt(), anyInt()))
            .thenReturn(searchResult);
        when(
            replacementService.findPageIdsReviewedByReplacement(any(WikipediaLanguage.class), anyString(), anyBoolean())
        )
            .thenReturn(Collections.emptyList());
        when(wikipediaService.getPageById(any(WikipediaPageId.class))).thenReturn(Optional.of(page));
        when(customReplacementFinderService.findCustomReplacements(any(FinderPage.class), any(CustomOptions.class)))
            .thenReturn(List.of(customRep));

        // First call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review1 = pageReviewCustomService.findRandomPageReview(options);
        assertFalse(review1.isEmpty());
        review1.ifPresent(
            r -> {
                assertEquals(lang, r.getPage().getLang());
                assertEquals(pageId, r.getPage().getId());
                assertEquals(content, r.getPage().getContent());
                assertEquals(1, r.getSearch().getNumPending());
            }
        );

        // Second call
        Optional<PageReview> review2 = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review2.isEmpty());

        // Verifications
        verify(wikipediaService).searchByText(lang, replacement, true, 0, CACHE_SIZE);
        verify(replacementService).findPageIdsReviewedByReplacement(lang, replacement, true);
        verify(wikipediaService).getPageById(wikipediaPageId);
        verify(replacementService, never()).insert(any(CustomEntity.class));
    }

    @Test
    void testResultWithNoReplacements() throws ReplacerException {
        final WikipediaLanguage lang = WikipediaLanguage.getDefault();
        final String replacement = "R";
        final String suggestion = "S";

        // Search in Wikipedia returns a result which is not reviewed yet but the page has no replacements
        // ==> Return an empty review
        // We don't insert any fake replacement in database.
        // Custom replacements are not re-indexed, so we wouldn't detect future changes in the page.

        final int pageId = 123;
        final String content = "A R";
        final WikipediaSearchResult searchResult = WikipediaSearchResult.of(1, List.of(pageId));
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
        when(wikipediaService.searchByText(any(WikipediaLanguage.class), anyString(), anyBoolean(), anyInt(), anyInt()))
            .thenReturn(searchResult);
        when(
            replacementService.findPageIdsReviewedByReplacement(any(WikipediaLanguage.class), anyString(), anyBoolean())
        )
            .thenReturn(Collections.emptyList());
        when(wikipediaService.getPageById(any(WikipediaPageId.class))).thenReturn(Optional.of(page));
        when(customReplacementFinderService.findCustomReplacements(any(FinderPage.class), any(CustomOptions.class)))
            .thenReturn(Collections.emptyList());

        // Only call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaService).searchByText(lang, replacement, true, 0, CACHE_SIZE);
        verify(replacementService).findPageIdsReviewedByReplacement(lang, replacement, true);
        verify(wikipediaService).getPageById(wikipediaPageId);
        verify(replacementService, never()).insert(any(CustomEntity.class));
    }

    @Test
    void testTwoResultsFirstReviewed() throws ReplacerException {
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
            .type(ReplacementType.CUSTOM)
            .subtype("R")
            .text("R")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("Z")))
            .build();
        final WikipediaSearchResult searchResult = WikipediaSearchResult.of(2, List.of(pageId1, pageId2));
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
        when(wikipediaService.searchByText(any(WikipediaLanguage.class), anyString(), anyBoolean(), anyInt(), anyInt()))
            .thenReturn(searchResult);
        when(
            replacementService.findPageIdsReviewedByReplacement(any(WikipediaLanguage.class), anyString(), anyBoolean())
        )
            .thenReturn(List.of(pageId1));
        when(wikipediaService.getPageById(any(WikipediaPageId.class))).thenReturn(Optional.of(page));
        when(customReplacementFinderService.findCustomReplacements(any(FinderPage.class), any(CustomOptions.class)))
            .thenReturn(List.of(customRep));

        // Only call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        assertFalse(review.isEmpty());
        review.ifPresent(
            r -> {
                assertEquals(lang, r.getPage().getLang());
                assertEquals(pageId2, r.getPage().getId());
                assertEquals(content, r.getPage().getContent());
                assertEquals(1, r.getSearch().getNumPending());
            }
        );

        // Verifications
        verify(wikipediaService).searchByText(lang, replacement, true, 0, CACHE_SIZE);
        verify(replacementService).findPageIdsReviewedByReplacement(lang, replacement, true);
        verify(wikipediaService).getPageById(wikipediaPageId2);
        verify(replacementService, never()).insert(any(CustomEntity.class));
    }

    @Test
    void testSeveralResultsAllReviewed() throws ReplacerException {
        final WikipediaLanguage lang = WikipediaLanguage.getDefault();
        final String replacement = "R";
        final String suggestion = "S";

        // Search in Wikipedia returns 4 results (pagination = 3) all of them reviewed in database
        // so we perform two calls to Wikipedia search
        // ==> Return an empty review

        // Mocks
        when(wikipediaService.searchByText(any(WikipediaLanguage.class), anyString(), anyBoolean(), anyInt(), anyInt()))
            .thenReturn(WikipediaSearchResult.of(4, List.of(12, 23, 34)))
            .thenReturn(WikipediaSearchResult.of(4, List.of(45)));
        when(
            replacementService.findPageIdsReviewedByReplacement(any(WikipediaLanguage.class), anyString(), anyBoolean())
        )
            .thenReturn(List.of(12, 23, 34, 45));

        // Only call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaService).searchByText(lang, replacement, true, 0, CACHE_SIZE);
        verify(wikipediaService).searchByText(lang, replacement, true, 3, CACHE_SIZE);
        verify(replacementService).findPageIdsReviewedByReplacement(lang, replacement, true);
        verify(wikipediaService, never()).getPageById(any(WikipediaPageId.class));
        verify(replacementService, never()).insert(any(CustomEntity.class));
    }

    @Test
    void testSeveralResults() throws ReplacerException {
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
            .type(ReplacementType.CUSTOM)
            .subtype("R")
            .text("R")
            .suggestions(List.of(ReplacementSuggestion.ofNoComment("Z")))
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
        when(wikipediaService.searchByText(any(WikipediaLanguage.class), anyString(), anyBoolean(), anyInt(), anyInt()))
            .thenReturn(WikipediaSearchResult.of(4, List.of(1, 2, 3))) // Call 1
            .thenReturn(WikipediaSearchResult.of(2, List.of(2, 4))) // Call 4
            .thenReturn(WikipediaSearchResult.of(2, List.of(2, 4))); // Call 5

        when(
            replacementService.findPageIdsReviewedByReplacement(any(WikipediaLanguage.class), anyString(), anyBoolean())
        )
            .thenReturn(Collections.emptyList()) // Call 1
            .thenReturn(List.of(1, 2, 3)) // Call 4
            .thenReturn(List.of(1, 2, 3, 4)); // Call 5

        when(wikipediaService.getPageById(any(WikipediaPageId.class)))
            .thenReturn(Optional.of(pages.get(1))) // Call 1
            .thenReturn(Optional.of(pages.get(2))) // Call 2
            .thenReturn(Optional.of(pages.get(3))) // Call 3
            .thenReturn(Optional.of(pages.get(4))); // Call 4

        when(customReplacementFinderService.findCustomReplacements(any(FinderPage.class), any(CustomOptions.class)))
            .thenReturn(List.of(customRep));

        // We cannot use the same options object for all calls as it is mutable (and mutated)
        // Call 1
        PageReviewOptions options1 = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options1);
        assertFalse(review.isEmpty());
        review.ifPresent(
            r -> {
                assertEquals(1, r.getPage().getId());
                assertEquals(4, r.getSearch().getNumPending());
            }
        );
        // Cache: 2, 3

        // Call 2
        PageReviewOptions options2 = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        review = pageReviewCustomService.findRandomPageReview(options2);
        assertFalse(review.isEmpty());
        review.ifPresent(
            r -> {
                assertEquals(2, r.getPage().getId());
                assertEquals(3, r.getSearch().getNumPending());
            }
        );
        // Cache: 3

        // Call 3
        PageReviewOptions options3 = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        review = pageReviewCustomService.findRandomPageReview(options3);
        assertFalse(review.isEmpty());
        review.ifPresent(
            r -> {
                assertEquals(3, r.getPage().getId());
                assertEquals(2, r.getSearch().getNumPending());
            }
        );
        // Cache: empty

        // Call 4
        PageReviewOptions options4 = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        review = pageReviewCustomService.findRandomPageReview(options4);
        assertFalse(review.isEmpty());
        review.ifPresent(
            r -> {
                assertEquals(4, r.getPage().getId());
                assertEquals(1, r.getSearch().getNumPending());
            }
        );
        // Cache: empty

        // Call 5: To start again after message of no more results
        PageReviewOptions options5 = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        review = pageReviewCustomService.findRandomPageReview(options5);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaService, times(2)).searchByText(lang, replacement, true, 0, CACHE_SIZE);
        verify(replacementService, times(2)).findPageIdsReviewedByReplacement(lang, replacement, true);
        verify(wikipediaService, times(4)).getPageById(any(WikipediaPageId.class));
    }

    @Test
    void testAllResultsWithoutReplacements() throws ReplacerException {
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
        when(wikipediaService.searchByText(any(WikipediaLanguage.class), anyString(), anyBoolean(), anyInt(), anyInt()))
            .thenReturn(WikipediaSearchResult.of(4, List.of(1, 2, 3)))
            .thenReturn(WikipediaSearchResult.of(4, List.of(4)))
            .thenReturn(WikipediaSearchResult.of(4, Collections.emptyList()));

        when(
            replacementService.findPageIdsReviewedByReplacement(any(WikipediaLanguage.class), anyString(), anyBoolean())
        )
            .thenReturn(Collections.emptyList());

        when(wikipediaService.getPageById(any(WikipediaPageId.class)))
            .thenReturn(Optional.of(pages.get(1))) // Call 1
            .thenReturn(Optional.of(pages.get(2))) // Call 2
            .thenReturn(Optional.of(pages.get(3))) // Call 3
            .thenReturn(Optional.of(pages.get(4))); // Call 4

        when(customReplacementFinderService.findCustomReplacements(any(FinderPage.class), any(CustomOptions.class)))
            .thenReturn(Collections.emptyList());

        // Only Call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaService).searchByText(lang, replacement, true, 0, CACHE_SIZE);
        verify(wikipediaService).searchByText(lang, replacement, true, CACHE_SIZE, CACHE_SIZE);
        verify(wikipediaService).searchByText(lang, replacement, true, 2 * CACHE_SIZE, CACHE_SIZE);
        verify(replacementService, times(2)).findPageIdsReviewedByReplacement(lang, replacement, true);
        verify(wikipediaService, times(4)).getPageById(any(WikipediaPageId.class));
    }

    @Test
    void testValidateCustomReplacement() {
        final WikipediaLanguage lang = WikipediaLanguage.getDefault();
        final ReplacementType simple = ReplacementType.MISSPELLING_SIMPLE;

        // Case-insensitive: accion||acción
        SimpleMisspelling misspelling1 = SimpleMisspelling.of("accion", false, "acción");
        when(customReplacementFinderService.findExistingMisspelling("accion", lang))
            .thenReturn(Optional.of(misspelling1));
        when(customReplacementFinderService.findExistingMisspelling("Accion", lang))
            .thenReturn(Optional.of(misspelling1));
        assertEquals(
            MisspellingType.ofEmpty(),
            pageReviewCustomService.validateCustomReplacement(lang, "accion", true)
        );
        assertEquals(
            MisspellingType.of(simple, "accion"),
            pageReviewCustomService.validateCustomReplacement(lang, "accion", false)
        );
        assertEquals(
            MisspellingType.ofEmpty(),
            pageReviewCustomService.validateCustomReplacement(lang, "Accion", true)
        );
        assertEquals(
            MisspellingType.of(simple, "accion"),
            pageReviewCustomService.validateCustomReplacement(lang, "Accion", false)
        );

        // Case-sensitive uppercase: Enero|cs|enero
        SimpleMisspelling misspelling2 = SimpleMisspelling.of("Enero", true, "enero");
        when(customReplacementFinderService.findExistingMisspelling("Enero", lang))
            .thenReturn(Optional.of(misspelling2));
        when(customReplacementFinderService.findExistingMisspelling("enero", lang)).thenReturn(Optional.empty());
        assertEquals(MisspellingType.ofEmpty(), pageReviewCustomService.validateCustomReplacement(lang, "enero", true));
        assertEquals(
            MisspellingType.ofEmpty(),
            pageReviewCustomService.validateCustomReplacement(lang, "enero", false)
        );
        assertEquals(
            MisspellingType.of(simple, "Enero"),
            pageReviewCustomService.validateCustomReplacement(lang, "Enero", true)
        );
        assertEquals(
            MisspellingType.ofEmpty(),
            pageReviewCustomService.validateCustomReplacement(lang, "Enero", false)
        );

        // Case-sensitive lowercase: madrid|cs|Madrid
        SimpleMisspelling misspelling3 = SimpleMisspelling.of("madrid", true, "Madrid");
        when(customReplacementFinderService.findExistingMisspelling("madrid", lang))
            .thenReturn(Optional.of(misspelling3));
        when(customReplacementFinderService.findExistingMisspelling("Madrid", lang)).thenReturn(Optional.empty());
        assertEquals(
            MisspellingType.of(simple, "madrid"),
            pageReviewCustomService.validateCustomReplacement(lang, "madrid", true)
        );
        assertEquals(
            MisspellingType.ofEmpty(),
            pageReviewCustomService.validateCustomReplacement(lang, "madrid", false)
        );
        assertEquals(
            MisspellingType.ofEmpty(),
            pageReviewCustomService.validateCustomReplacement(lang, "Madrid", true)
        );
        assertEquals(
            MisspellingType.ofEmpty(),
            pageReviewCustomService.validateCustomReplacement(lang, "Madrid", false)
        );
    }
}
