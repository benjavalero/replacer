package es.bvalero.replacer.page;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.common.WikipediaNamespace;
import es.bvalero.replacer.finder.common.FinderPage;
import es.bvalero.replacer.finder.replacement.CustomOptions;
import es.bvalero.replacer.finder.replacement.CustomReplacementFinderService;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import es.bvalero.replacer.replacement.ReplacementEntity;
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
            .findPageIdsReviewedByTypeAndSubtype(any(WikipediaLanguage.class), anyString(), anyString());
        verify(replacementService, never()).insert(any(ReplacementEntity.class));
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
            replacementService.findPageIdsReviewedByTypeAndSubtype(
                any(WikipediaLanguage.class),
                anyString(),
                anyString()
            )
        )
            .thenReturn(List.of(pageId));

        // Only one call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        Assertions.assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaService, times(1)).getPageIdsByStringMatch(lang, replacement, true, 0, CACHE_SIZE);
        verify(replacementService, times(1))
            .findPageIdsReviewedByTypeAndSubtype(lang, ReplacementType.CUSTOM, replacement);
        verify(replacementService, never()).insert(any(ReplacementEntity.class));
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
            replacementService.findPageIdsReviewedByTypeAndSubtype(
                any(WikipediaLanguage.class),
                anyString(),
                anyString()
            )
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
                Assertions.assertEquals(lang, r.getLang());
                Assertions.assertEquals(pageId, r.getId());
                Assertions.assertEquals(content, r.getContent());
                Assertions.assertEquals(1, r.getNumPending());
            }
        );

        // Second call
        Optional<PageReview> review2 = pageReviewCustomService.findRandomPageReview(options);
        Assertions.assertTrue(review2.isEmpty());

        // Verifications
        verify(wikipediaService, times(1)).getPageIdsByStringMatch(lang, replacement, true, 0, CACHE_SIZE);
        verify(replacementService, times(1))
            .findPageIdsReviewedByTypeAndSubtype(lang, ReplacementType.CUSTOM, replacement);
        verify(wikipediaService, times(1)).getPageById(pageId, lang);
        verify(replacementService, never()).insert(any(ReplacementEntity.class));
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
            replacementService.findPageIdsReviewedByTypeAndSubtype(
                any(WikipediaLanguage.class),
                anyString(),
                anyString()
            )
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
        verify(replacementService, times(1))
            .findPageIdsReviewedByTypeAndSubtype(lang, ReplacementType.CUSTOM, replacement);
        verify(wikipediaService, times(1)).getPageById(pageId, lang);
        verify(replacementService, never()).insert(any(ReplacementEntity.class));
    }

    @Test
    void testWtoResultsFirstReviewed() throws ReplacerException {
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
            replacementService.findPageIdsReviewedByTypeAndSubtype(
                any(WikipediaLanguage.class),
                anyString(),
                anyString()
            )
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
                Assertions.assertEquals(lang, r.getLang());
                Assertions.assertEquals(pageId2, r.getId());
                Assertions.assertEquals(content, r.getContent());
                Assertions.assertEquals(1, r.getNumPending());
            }
        );

        // Verifications
        verify(wikipediaService, times(1)).getPageIdsByStringMatch(lang, replacement, true, 0, CACHE_SIZE);
        verify(replacementService, times(1))
            .findPageIdsReviewedByTypeAndSubtype(lang, ReplacementType.CUSTOM, replacement);
        verify(wikipediaService, times(1)).getPageById(pageId2, lang);
        verify(replacementService, never()).insert(any(ReplacementEntity.class));
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
            replacementService.findPageIdsReviewedByTypeAndSubtype(
                any(WikipediaLanguage.class),
                anyString(),
                anyString()
            )
        )
            .thenReturn(List.of(12, 23, 34, 45));

        // Only call
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        Assertions.assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaService, times(1)).getPageIdsByStringMatch(lang, replacement, true, 0, CACHE_SIZE);
        verify(wikipediaService, times(1)).getPageIdsByStringMatch(lang, replacement, true, 3, CACHE_SIZE);
        verify(replacementService, times(1))
            .findPageIdsReviewedByTypeAndSubtype(lang, ReplacementType.CUSTOM, replacement);
        verify(wikipediaService, times(0)).getPageById(anyInt(), any(WikipediaLanguage.class));
        verify(replacementService, never()).insert(any(ReplacementEntity.class));
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
            replacementService.findPageIdsReviewedByTypeAndSubtype(
                any(WikipediaLanguage.class),
                anyString(),
                anyString()
            )
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

        // Call 1
        PageReviewOptions options = PageReviewOptions.ofCustom(lang, replacement, suggestion, true);
        Optional<PageReview> review = pageReviewCustomService.findRandomPageReview(options);
        Assertions.assertFalse(review.isEmpty());
        review.ifPresent(
            r -> {
                Assertions.assertEquals(1, r.getId());
                Assertions.assertEquals(4, r.getNumPending());
            }
        );
        // Cache: 2, 3

        // Call 2
        review = pageReviewCustomService.findRandomPageReview(options);
        Assertions.assertFalse(review.isEmpty());
        review.ifPresent(
            r -> {
                Assertions.assertEquals(2, r.getId());
                Assertions.assertEquals(3, r.getNumPending());
            }
        );
        // Cache: 3

        // Call 3
        review = pageReviewCustomService.findRandomPageReview(options);
        Assertions.assertFalse(review.isEmpty());
        review.ifPresent(
            r -> {
                Assertions.assertEquals(3, r.getId());
                Assertions.assertEquals(2, r.getNumPending());
            }
        );
        // Cache: empty

        // Call 4
        review = pageReviewCustomService.findRandomPageReview(options);
        Assertions.assertFalse(review.isEmpty());
        review.ifPresent(
            r -> {
                Assertions.assertEquals(4, r.getId());
                Assertions.assertEquals(1, r.getNumPending());
            }
        );

        // Call 5: To start again after message of no more results
        review = pageReviewCustomService.findRandomPageReview(options);
        Assertions.assertTrue(review.isEmpty());

        // Verifications
        verify(wikipediaService, times(2)).getPageIdsByStringMatch(lang, replacement, true, 0, CACHE_SIZE);
        verify(replacementService, times(2))
            .findPageIdsReviewedByTypeAndSubtype(lang, ReplacementType.CUSTOM, replacement);
        verify(wikipediaService, times(4)).getPageById(anyInt(), any(WikipediaLanguage.class));
    }
}
