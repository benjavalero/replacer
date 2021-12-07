package es.bvalero.replacer.page.review;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.page.index.PageIndexStatus;
import es.bvalero.replacer.page.index.PageIndexer;
import es.bvalero.replacer.replacement.count.ReplacementCountService;
import es.bvalero.replacer.repository.PageRepository;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageReviewTypeSubtypeFinderTest {

    private final int randomId = 1;
    private final int randomId2 = 2;
    private final String content = "XYZ";
    private final String content2 = "Y";
    private final WikipediaPageId randomPageId = WikipediaPageId.of(WikipediaLanguage.getDefault(), randomId);
    private final WikipediaPageId randomPageId2 = WikipediaPageId.of(WikipediaLanguage.getDefault(), randomId2);
    private final WikipediaPage page = WikipediaPage
        .builder()
        .id(randomPageId)
        .namespace(WikipediaNamespace.ARTICLE)
        .title("Title1")
        .content(content)
        .lastUpdate(LocalDateTime.now())
        .build();
    private final WikipediaPage page2 = WikipediaPage
        .builder()
        .id(randomPageId2)
        .namespace(WikipediaNamespace.ANNEX)
        .title("Title2")
        .content(content2)
        .lastUpdate(LocalDateTime.now())
        .build();
    private final int offset = 1;
    private final Replacement replacement = Replacement
        .builder()
        .start(offset)
        .type(ReplacementType.MISSPELLING_SIMPLE)
        .subtype("Y")
        .text("Y")
        .suggestions(List.of(Suggestion.ofNoComment("Z")))
        .build();
    private final List<Replacement> replacements = Collections.singletonList(replacement);
    private final PageReviewOptions options = PageReviewOptions.ofTypeSubtype(
        ReplacementType.MISSPELLING_SIMPLE.getLabel(),
        "Y"
    );
    private final PageReviewOptions options2 = PageReviewOptions.ofTypeSubtype(
        ReplacementType.MISSPELLING_COMPOSED.getLabel(),
        "B"
    );

    @Mock
    private PageRepository pageRepository;

    @Mock
    private ReplacementCountService replacementCountService;

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private PageIndexer pageIndexer;

    @Mock
    private PageReviewSectionFinder pageReviewSectionFinder;

    @InjectMocks
    private PageReviewTypeSubtypeFinder pageReviewTypeSubtypeService;

    @BeforeEach
    public void setUp() {
        pageReviewTypeSubtypeService = new PageReviewTypeSubtypeFinder();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindRandomPageToReviewTypeNotFiltered() throws ReplacerException {
        // 1 result in DB
        when(pageRepository.findPageIdsToReviewByType(any(WikipediaLanguage.class), anyString(), anyString(), anyInt()))
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)))
            .thenReturn(Collections.emptyList());

        // The page exists in Wikipedia
        when(wikipediaService.getPageById(randomPageId)).thenReturn(Optional.of(page));

        when(pageIndexer.indexPageReplacements(page))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_INDEXED, replacements));

        Optional<PageReview> review = pageReviewTypeSubtypeService.findRandomPageReview(options2);

        verify(pageIndexer).indexPageReplacements(page);

        assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomPageToReviewTypeFiltered() throws ReplacerException {
        // 1 result in DB
        when(pageRepository.findPageIdsToReviewByType(any(WikipediaLanguage.class), anyString(), anyString(), anyInt()))
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)));

        // The page exists in Wikipedia
        when(wikipediaService.getPageById(randomPageId)).thenReturn(Optional.of(page));

        when(pageIndexer.indexPageReplacements(page))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_INDEXED, replacements));

        Optional<PageReview> review = pageReviewTypeSubtypeService.findRandomPageReview(options);

        verify(pageIndexer).indexPageReplacements(page);

        assertTrue(review.isPresent());
        assertEquals(randomId, review.get().getPage().getId().getPageId());
    }

    @Test
    void testFindRandomPageToReviewNoTypeAndThenFiltered() throws ReplacerException {
        // 1. Find the random page 1 by type. In DB there exists also the page 2.
        // 2. Find the random page 2 by no type. The page 2 is supposed to be removed from all the caches.
        // 3. Find a random page by type. In DB there is no page.

        // 2 results in DB by type, no results the second time.
        when(pageRepository.findPageIdsToReviewByType(any(WikipediaLanguage.class), anyString(), anyString(), anyInt()))
            .thenReturn(new ArrayList<>(Arrays.asList(randomId, randomId2)))
            .thenReturn(Collections.emptyList());
        // 1 result in DB by no type
        when(pageRepository.findPageIdsToReview(any(WikipediaLanguage.class), anyInt()))
            .thenReturn(new ArrayList<>(Collections.singletonList(randomId2)));

        // The pages exist in Wikipedia
        when(wikipediaService.getPageById(randomPageId)).thenReturn(Optional.of(page));
        when(wikipediaService.getPageById(randomPageId2)).thenReturn(Optional.of(page2));

        when(pageIndexer.indexPageReplacements(any(WikipediaPage.class)))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_INDEXED, replacements));

        Optional<PageReview> review = pageReviewTypeSubtypeService.findRandomPageReview(options);
        assertTrue(review.isPresent());
        assertEquals(randomId, review.get().getPage().getId().getPageId());

        review = pageReviewTypeSubtypeService.findRandomPageReview(options);
        assertTrue(review.isPresent());
        assertEquals(randomId2, review.get().getPage().getId().getPageId());

        review = pageReviewTypeSubtypeService.findRandomPageReview(options);
        assertFalse(review.isPresent());
    }

    @Test
    void testPageReviewWithSection() throws ReplacerException {
        final int sectionId = 1;

        // The page exists in Wikipedia
        when(wikipediaService.getPageById(randomPageId)).thenReturn(Optional.of(page));

        when(pageIndexer.indexPageReplacements(page))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_INDEXED, replacements));

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
        Long numPending = 5L;
        PageReview sectionReview = PageReview.of(page, section, replacements, numPending);
        when(pageReviewSectionFinder.findPageReviewSection(any(PageReview.class)))
            .thenReturn(Optional.of(sectionReview));

        Optional<PageReview> review = pageReviewTypeSubtypeService.getPageReview(randomId, options);

        assertTrue(review.isPresent());
        review.ifPresent(
            rev -> {
                assertEquals(randomId, rev.getPage().getId().getPageId());
                assertEquals(replacements.size(), rev.getReplacements().size());
                assertNotNull(rev.getSection());
                assertNotNull(rev.getSection().getIndex());
                assertEquals(sectionId, rev.getSection().getIndex().intValue());
                assertEquals(numPending, rev.getNumPending());
            }
        );
    }

    @Test
    void testPageReviewWithNoSection() throws ReplacerException {
        // The page exists in Wikipedia
        when(wikipediaService.getPageById(randomPageId)).thenReturn(Optional.of(page));

        when(pageIndexer.indexPageReplacements(page))
            .thenReturn(PageIndexResult.ofEmpty(PageIndexStatus.PAGE_INDEXED, replacements));

        // The page has no sections
        when(pageReviewSectionFinder.findPageReviewSection(any(PageReview.class))).thenReturn(Optional.empty());

        // Load the cache in order to find the total results
        pageReviewTypeSubtypeService.loadCache(options);

        Optional<PageReview> review = pageReviewTypeSubtypeService.getPageReview(randomId, options);

        assertTrue(review.isPresent());
        review.ifPresent(
            rev -> {
                assertEquals(randomId, rev.getPage().getId().getPageId());
                assertEquals(replacements.size(), rev.getReplacements().size());
                assertNull(rev.getSection());
            }
        );
    }
}
