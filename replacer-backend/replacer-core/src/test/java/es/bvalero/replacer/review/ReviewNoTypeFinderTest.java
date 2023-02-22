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
import es.bvalero.replacer.user.UserId;
import es.bvalero.replacer.user.UserRightsService;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReviewNoTypeFinderTest {

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
        .text("Y")
        .type(ReplacementType.of(ReplacementKind.STYLE, "Y"))
        .suggestions(List.of(Suggestion.ofNoComment("Z")))
        .build();
    private final List<Replacement> replacements = Collections.singletonList(replacement);
    private final UserId userId = UserId.of(WikipediaLanguage.getDefault(), "A");
    private final ReviewOptions options = ReviewOptions.ofNoType(userId);

    @Mock
    private PageService pageService;

    @Mock
    private PageCountService pageCountService;

    @Mock
    private WikipediaPageRepository wikipediaPageRepository;

    @Mock
    private PageIndexService pageIndexService;

    @Mock
    private ReviewSectionFinder reviewSectionFinder;

    @Mock
    private UserRightsService userRightsService;

    @InjectMocks
    private ReviewNoTypeFinder pageReviewNoTypeService;

    @BeforeEach
    public void setUp() {
        pageReviewNoTypeService = new ReviewNoTypeFinder();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindRandomPageToReviewNoTypeNoResultInDb() {
        // No results in DB
        when(pageService.findPagesToReviewByNoType(any(WikipediaLanguage.class), anyInt()))
            .thenReturn(Collections.emptyList());

        Optional<Review> review = pageReviewNoTypeService.findRandomPageReview(options);

        assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomPageToReviewNoTypeNotInWikipedia() {
        // 1 result in DB
        when(pageService.findPagesToReviewByNoType(any(WikipediaLanguage.class), anyInt()))
            .thenReturn(new ArrayList<>(Collections.singleton(randomPageKey)))
            .thenReturn(Collections.emptyList());

        // The page doesn't exist in Wikipedia
        when(wikipediaPageRepository.findByKey(randomPageKey)).thenReturn(Optional.empty());

        Optional<Review> review = pageReviewNoTypeService.findRandomPageReview(options);

        assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomPageToReviewNoTypeWithReplacements() {
        // 1 result in DB
        when(pageCountService.countPagesToReviewByNoType(any(WikipediaLanguage.class))).thenReturn(1);
        when(pageService.findPagesToReviewByNoType(any(WikipediaLanguage.class), anyInt()))
            .thenReturn(new ArrayList<>(Collections.singleton(randomPageKey)));

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
        when(pageCountService.countPagesToReviewByNoType(any(WikipediaLanguage.class))).thenReturn(1);
        when(pageService.findPagesToReviewByNoType(any(WikipediaLanguage.class), anyInt()))
            .thenReturn(new ArrayList<>(Collections.singleton(randomPageKey)))
            .thenReturn(Collections.emptyList());

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
        when(pageCountService.countPagesToReviewByNoType(any(WikipediaLanguage.class))).thenReturn(2);
        when(pageService.findPagesToReviewByNoType(any(WikipediaLanguage.class), anyInt()))
            .thenReturn(List.of(randomPageKey, randomPageKey2));

        // Only the page 2 exists in Wikipedia
        when(wikipediaPageRepository.findByKey(randomPageKey)).thenReturn(Optional.empty());
        when(wikipediaPageRepository.findByKey(randomPageKey2)).thenReturn(Optional.of(page2));

        when(pageIndexService.indexPage(page2)).thenReturn(PageIndexResult.ofIndexed(replacements));

        Optional<Review> review = pageReviewNoTypeService.findRandomPageReview(options);

        verify(pageIndexService).indexPage(page2);

        assertTrue(review.isPresent());
        assertEquals(randomId2, review.get().getPage().getPageId());
    }
}
