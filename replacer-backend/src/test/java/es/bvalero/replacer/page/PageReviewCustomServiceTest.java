package es.bvalero.replacer.page;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.common.WikipediaNamespace;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.replacement.*;
import es.bvalero.replacer.replacement.CustomEntity;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.*;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class PageReviewCustomServiceTest {

    private static final int CACHE_SIZE = 3;

    @Mock
    private ReplacementService replacementService;

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private CustomReplacementFinderService customReplacementFinderService;

    @Mock
    private SectionReviewService sectionReviewService;

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

        when(
            wikipediaService.getPageIdsByStringMatch(
                any(WikipediaLanguage.class),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()
            )
        )
            .thenReturn(WikipediaSearchResult.ofEmpty());

        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);

        Assertions.assertTrue(review.isEmpty());

        verify(wikipediaService, times(1)).getPageIdsByStringMatch(lang, replacement, true, 0, CACHE_SIZE);
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
        when(
            wikipediaService.getPageIdsByStringMatch(
                any(WikipediaLanguage.class),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()
            )
        )
            .thenReturn(searchResult);
        when(
            replacementService.findPageIdsReviewedByReplacement(any(WikipediaLanguage.class), anyString(), anyBoolean())
        )
            .thenReturn(List.of(pageId));

        // Only one call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        Assertions.assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaService, times(1)).getPageIdsByStringMatch(lang, replacement, true, 0, CACHE_SIZE);
        verify(replacementService, times(1)).findPageIdsReviewedByReplacement(lang, replacement, true);
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
        final Replacement customRep = Replacement.builder().start(2).text("R").build();
        final WikipediaSearchResult searchResult = WikipediaSearchResult.of(1, List.of(pageId));
        final WikipediaPage page = WikipediaPage
            .builder()
            .lang(lang)
            .id(pageId)
            .namespace(WikipediaNamespace.ARTICLE)
            .content(content)
            .build();

        // Mocks
        when(
            wikipediaService.getPageIdsByStringMatch(
                any(WikipediaLanguage.class),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()
            )
        )
            .thenReturn(searchResult);
        when(
            replacementService.findPageIdsReviewedByReplacement(any(WikipediaLanguage.class), anyString(), anyBoolean())
        )
            .thenReturn(Collections.emptyList());
        when(wikipediaService.getPageById(anyInt(), any(WikipediaLanguage.class))).thenReturn(Optional.of(page));
        when(customReplacementFinderService.findCustomReplacements(any(FinderPage.class), any(CustomOptions.class)))
            .thenReturn(List.of(customRep));

        // First call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review1 = pageReviewCustomService.findRandomPageReview(options);
        Assertions.assertFalse(review1.isEmpty());
        review1.ifPresent(
            r -> {
                Assertions.assertEquals(lang, r.getPage().getLang());
                Assertions.assertEquals(pageId, r.getPage().getId());
                Assertions.assertEquals(content, r.getPage().getContent());
                Assertions.assertEquals(1, r.getSearch().getNumPending());
            }
        );

        // Second call
        Optional<PageReview> review2 = pageReviewCustomService.findRandomPageReview(options);
        Assertions.assertTrue(review2.isEmpty());

        // Verifications
        verify(wikipediaService, times(1)).getPageIdsByStringMatch(lang, replacement, true, 0, CACHE_SIZE);
        verify(replacementService, times(1)).findPageIdsReviewedByReplacement(lang, replacement, true);
        verify(wikipediaService, times(1)).getPageById(pageId, lang);
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
        // Custom replacements are not re-indexed so we wouldn't detect future changes in the page.

        final int pageId = 123;
        final String content = "A R";
        final WikipediaSearchResult searchResult = WikipediaSearchResult.of(1, List.of(pageId));
        final WikipediaPage page = WikipediaPage
            .builder()
            .lang(lang)
            .id(pageId)
            .namespace(WikipediaNamespace.ARTICLE)
            .content(content)
            .build();

        // Mocks
        when(
            wikipediaService.getPageIdsByStringMatch(
                any(WikipediaLanguage.class),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()
            )
        )
            .thenReturn(searchResult);
        when(
            replacementService.findPageIdsReviewedByReplacement(any(WikipediaLanguage.class), anyString(), anyBoolean())
        )
            .thenReturn(Collections.emptyList());
        when(wikipediaService.getPageById(anyInt(), any(WikipediaLanguage.class))).thenReturn(Optional.of(page));
        when(customReplacementFinderService.findCustomReplacements(any(FinderPage.class), any(CustomOptions.class)))
            .thenReturn(Collections.emptyList());

        // Only call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        Assertions.assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaService, times(1)).getPageIdsByStringMatch(lang, replacement, true, 0, CACHE_SIZE);
        verify(replacementService, times(1)).findPageIdsReviewedByReplacement(lang, replacement, true);
        verify(wikipediaService, times(1)).getPageById(pageId, lang);
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
        final Replacement customRep = Replacement.builder().start(2).text("R").build();
        final WikipediaSearchResult searchResult = WikipediaSearchResult.of(2, List.of(pageId1, pageId2));
        final WikipediaPage page = WikipediaPage
            .builder()
            .lang(lang)
            .id(pageId2)
            .namespace(WikipediaNamespace.ARTICLE)
            .content(content)
            .build();

        // Mocks
        when(
            wikipediaService.getPageIdsByStringMatch(
                any(WikipediaLanguage.class),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()
            )
        )
            .thenReturn(searchResult);
        when(
            replacementService.findPageIdsReviewedByReplacement(any(WikipediaLanguage.class), anyString(), anyBoolean())
        )
            .thenReturn(List.of(pageId1));
        when(wikipediaService.getPageById(anyInt(), any(WikipediaLanguage.class))).thenReturn(Optional.of(page));
        when(customReplacementFinderService.findCustomReplacements(any(FinderPage.class), any(CustomOptions.class)))
            .thenReturn(List.of(customRep));

        // Only call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        Assertions.assertFalse(review.isEmpty());
        review.ifPresent(
            r -> {
                Assertions.assertEquals(lang, r.getPage().getLang());
                Assertions.assertEquals(pageId2, r.getPage().getId());
                Assertions.assertEquals(content, r.getPage().getContent());
                Assertions.assertEquals(1, r.getSearch().getNumPending());
            }
        );

        // Verifications
        verify(wikipediaService, times(1)).getPageIdsByStringMatch(lang, replacement, true, 0, CACHE_SIZE);
        verify(replacementService, times(1)).findPageIdsReviewedByReplacement(lang, replacement, true);
        verify(wikipediaService, times(1)).getPageById(pageId2, lang);
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
        when(
            wikipediaService.getPageIdsByStringMatch(
                any(WikipediaLanguage.class),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()
            )
        )
            .thenReturn(WikipediaSearchResult.of(4, List.of(12, 23, 34)))
            .thenReturn(WikipediaSearchResult.of(4, List.of(45)));
        when(
            replacementService.findPageIdsReviewedByReplacement(any(WikipediaLanguage.class), anyString(), anyBoolean())
        )
            .thenReturn(List.of(12, 23, 34, 45));

        // Only call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        Assertions.assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaService, times(1)).getPageIdsByStringMatch(lang, replacement, true, 0, CACHE_SIZE);
        verify(wikipediaService, times(1)).getPageIdsByStringMatch(lang, replacement, true, 3, CACHE_SIZE);
        verify(replacementService, times(1)).findPageIdsReviewedByReplacement(lang, replacement, true);
        verify(wikipediaService, times(0)).getPageById(anyInt(), any(WikipediaLanguage.class));
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
        final Replacement customRep = Replacement.builder().start(2).text("R").build();
        final Map<Integer, WikipediaPage> pages = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            pages.put(
                i,
                WikipediaPage.builder().lang(lang).id(i).namespace(WikipediaNamespace.ARTICLE).content(content).build()
            );
        }

        // Mocks
        when(
            wikipediaService.getPageIdsByStringMatch(
                any(WikipediaLanguage.class),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()
            )
        )
            .thenReturn(WikipediaSearchResult.of(4, List.of(1, 2, 3))) // Call 1
            .thenReturn(WikipediaSearchResult.of(2, List.of(2, 4))) // Call 4
            .thenReturn(WikipediaSearchResult.of(2, List.of(2, 4))); // Call 5

        when(
            replacementService.findPageIdsReviewedByReplacement(any(WikipediaLanguage.class), anyString(), anyBoolean())
        )
            .thenReturn(Collections.emptyList()) // Call 1
            .thenReturn(List.of(1, 2, 3)) // Call 4
            .thenReturn(List.of(1, 2, 3, 4)); // Call 5

        when(wikipediaService.getPageById(anyInt(), any(WikipediaLanguage.class)))
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
        Assertions.assertFalse(review.isEmpty());
        review.ifPresent(
            r -> {
                Assertions.assertEquals(1, r.getPage().getId());
                Assertions.assertEquals(4, r.getSearch().getNumPending());
            }
        );
        // Cache: 2, 3

        // Call 2
        PageReviewOptions options2 = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        review = pageReviewCustomService.findRandomPageReview(options2);
        Assertions.assertFalse(review.isEmpty());
        review.ifPresent(
            r -> {
                Assertions.assertEquals(2, r.getPage().getId());
                Assertions.assertEquals(3, r.getSearch().getNumPending());
            }
        );
        // Cache: 3

        // Call 3
        PageReviewOptions options3 = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        review = pageReviewCustomService.findRandomPageReview(options3);
        Assertions.assertFalse(review.isEmpty());
        review.ifPresent(
            r -> {
                Assertions.assertEquals(3, r.getPage().getId());
                Assertions.assertEquals(2, r.getSearch().getNumPending());
            }
        );
        // Cache: empty

        // Call 4
        PageReviewOptions options4 = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        review = pageReviewCustomService.findRandomPageReview(options4);
        Assertions.assertFalse(review.isEmpty());
        review.ifPresent(
            r -> {
                Assertions.assertEquals(4, r.getPage().getId());
                Assertions.assertEquals(1, r.getSearch().getNumPending());
            }
        );
        // Cache: empty

        // Call 5: To start again after message of no more results
        PageReviewOptions options5 = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        review = pageReviewCustomService.findRandomPageReview(options5);
        Assertions.assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaService, times(2)).getPageIdsByStringMatch(lang, replacement, true, 0, CACHE_SIZE);
        verify(replacementService, times(2)).findPageIdsReviewedByReplacement(lang, replacement, true);
        verify(wikipediaService, times(4)).getPageById(anyInt(), any(WikipediaLanguage.class));
    }

    @Test
    void testAllResultsWithoutReplacements() throws ReplacerException {
        final WikipediaLanguage lang = WikipediaLanguage.getDefault();
        final String replacement = "R";
        final String suggestion = "S";

        // 4 Wikipedia results all without repalcements

        final String content = "A R";
        final Map<Integer, WikipediaPage> pages = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            pages.put(
                i,
                WikipediaPage.builder().lang(lang).id(i).namespace(WikipediaNamespace.ARTICLE).content(content).build()
            );
        }

        // Mocks
        when(
            wikipediaService.getPageIdsByStringMatch(
                any(WikipediaLanguage.class),
                anyString(),
                anyBoolean(),
                anyInt(),
                anyInt()
            )
        )
            .thenReturn(WikipediaSearchResult.of(4, List.of(1, 2, 3)))
            .thenReturn(WikipediaSearchResult.of(4, List.of(4)))
            .thenReturn(WikipediaSearchResult.of(4, Collections.emptyList()));

        when(
            replacementService.findPageIdsReviewedByReplacement(any(WikipediaLanguage.class), anyString(), anyBoolean())
        )
            .thenReturn(Collections.emptyList());

        when(wikipediaService.getPageById(anyInt(), any(WikipediaLanguage.class)))
            .thenReturn(Optional.of(pages.get(1))) // Call 1
            .thenReturn(Optional.of(pages.get(2))) // Call 2
            .thenReturn(Optional.of(pages.get(3))) // Call 3
            .thenReturn(Optional.of(pages.get(4))); // Call 4

        when(customReplacementFinderService.findCustomReplacements(any(FinderPage.class), any(CustomOptions.class)))
            .thenReturn(Collections.emptyList());

        // Only Call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        Assertions.assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaService, times(1)).getPageIdsByStringMatch(lang, replacement, true, 0, CACHE_SIZE);
        verify(wikipediaService, times(1)).getPageIdsByStringMatch(lang, replacement, true, CACHE_SIZE, CACHE_SIZE);
        verify(wikipediaService, times(1)).getPageIdsByStringMatch(lang, replacement, true, 2 * CACHE_SIZE, CACHE_SIZE);
        verify(replacementService, times(2)).findPageIdsReviewedByReplacement(lang, replacement, true);
        verify(wikipediaService, times(4)).getPageById(anyInt(), any(WikipediaLanguage.class));
    }

    @Test
    void testValidateCustomReplacement() {
        final WikipediaLanguage lang = WikipediaLanguage.getDefault();
        final String simple = ReplacementType.MISSPELLING_SIMPLE;

        // Case-insensitive: accion||acción
        Misspelling misspelling1 = Misspelling.of(simple, "accion", false, "acción");
        Mockito
            .when(customReplacementFinderService.findExistingMisspelling("accion", lang))
            .thenReturn(Optional.of(misspelling1));
        Mockito
            .when(customReplacementFinderService.findExistingMisspelling("Accion", lang))
            .thenReturn(Optional.of(misspelling1));
        Assertions.assertEquals(
            MisspellingType.ofEmpty(),
            pageReviewCustomService.validateCustomReplacement(lang, "accion", true)
        );
        Assertions.assertEquals(
            MisspellingType.of(simple, "accion"),
            pageReviewCustomService.validateCustomReplacement(lang, "accion", false)
        );
        Assertions.assertEquals(
            MisspellingType.ofEmpty(),
            pageReviewCustomService.validateCustomReplacement(lang, "Accion", true)
        );
        Assertions.assertEquals(
            MisspellingType.of(simple, "accion"),
            pageReviewCustomService.validateCustomReplacement(lang, "Accion", false)
        );

        // Case-sensitive uppercase: Enero|cs|enero
        Misspelling misspelling2 = Misspelling.of(simple, "Enero", true, "enero");
        Mockito
            .when(customReplacementFinderService.findExistingMisspelling("Enero", lang))
            .thenReturn(Optional.of(misspelling2));
        Mockito
            .when(customReplacementFinderService.findExistingMisspelling("enero", lang))
            .thenReturn(Optional.empty());
        Assertions.assertEquals(
            MisspellingType.ofEmpty(),
            pageReviewCustomService.validateCustomReplacement(lang, "enero", true)
        );
        Assertions.assertEquals(
            MisspellingType.ofEmpty(),
            pageReviewCustomService.validateCustomReplacement(lang, "enero", false)
        );
        Assertions.assertEquals(
            MisspellingType.of(simple, "Enero"),
            pageReviewCustomService.validateCustomReplacement(lang, "Enero", true)
        );
        Assertions.assertEquals(
            MisspellingType.ofEmpty(),
            pageReviewCustomService.validateCustomReplacement(lang, "Enero", false)
        );

        // Case-sensitive lowercase: madrid|cs|Madrid
        Misspelling misspelling3 = Misspelling.of(simple, "madrid", true, "Madrid");
        Mockito
            .when(customReplacementFinderService.findExistingMisspelling("madrid", lang))
            .thenReturn(Optional.of(misspelling3));
        Mockito
            .when(customReplacementFinderService.findExistingMisspelling("Madrid", lang))
            .thenReturn(Optional.empty());
        Assertions.assertEquals(
            MisspellingType.of(simple, "madrid"),
            pageReviewCustomService.validateCustomReplacement(lang, "madrid", true)
        );
        Assertions.assertEquals(
            MisspellingType.ofEmpty(),
            pageReviewCustomService.validateCustomReplacement(lang, "madrid", false)
        );
        Assertions.assertEquals(
            MisspellingType.ofEmpty(),
            pageReviewCustomService.validateCustomReplacement(lang, "Madrid", true)
        );
        Assertions.assertEquals(
            MisspellingType.ofEmpty(),
            pageReviewCustomService.validateCustomReplacement(lang, "Madrid", false)
        );
    }
}
