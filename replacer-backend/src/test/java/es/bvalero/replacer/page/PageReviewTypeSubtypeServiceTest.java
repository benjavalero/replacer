package es.bvalero.replacer.page;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.domain.ReplacerException;
import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.finder.replacement.ReplacementSuggestion;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import es.bvalero.replacer.page.index.IndexablePage;
import es.bvalero.replacer.page.index.IndexablePageValidator;
import es.bvalero.replacer.page.index.PageIndexHelper;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;

class PageReviewTypeSubtypeServiceTest {

    private final int randomId = 1;
    private final int randomId2 = 2;
    private final String content = "XYZ";
    private final String content2 = "Y";
    private final WikipediaPage page = WikipediaPage
        .builder()
        .id(randomId)
        .lang(WikipediaLanguage.SPANISH)
        .namespace(WikipediaNamespace.ARTICLE)
        .content(content)
        .lastUpdate(LocalDateTime.now())
        .queryTimestamp(LocalDateTime.now())
        .build();
    private final WikipediaPage page2 = WikipediaPage
        .builder()
        .id(randomId2)
        .lang(WikipediaLanguage.SPANISH)
        .namespace(WikipediaNamespace.ANNEX)
        .content(content2)
        .lastUpdate(LocalDateTime.now())
        .queryTimestamp(LocalDateTime.now())
        .build();
    private final int offset = 1;
    private final Replacement replacement = Replacement
        .builder()
        .start(offset)
        .type(ReplacementType.MISSPELLING_SIMPLE)
        .subtype("Y")
        .text("Y")
        .suggestions(List.of(ReplacementSuggestion.ofNoComment("Z")))
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
    private ReplacementService replacementService;

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private ReplacementFinderService replacementFinderService;

    @Mock
    private PageIndexHelper pageIndexHelper;

    @Mock
    private SectionReviewService sectionReviewService;

    @Mock
    private IndexablePageValidator indexablePageValidator;

    @InjectMocks
    private PageReviewTypeSubtypeService pageReviewTypeSubtypeService;

    @BeforeEach
    public void setUp() {
        pageReviewTypeSubtypeService = new PageReviewTypeSubtypeService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindRandomPageToReviewTypeNotFiltered() throws ReplacerException {
        // 1 result in DB
        when(
            replacementService.findRandomPageIdsToBeReviewedBySubtype(
                any(WikipediaLanguage.class),
                anyString(),
                anyString(),
                any(PageRequest.class)
            )
        )
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)))
            .thenReturn(Collections.emptyList());

        // The page exists in Wikipedia
        when(wikipediaService.getPageById(WikipediaLanguage.SPANISH, randomId)).thenReturn(Optional.of(page));

        // The page contains replacements
        when(replacementFinderService.find(pageReviewTypeSubtypeService.convertToFinderPage(page)))
            .thenReturn(replacements);

        Optional<PageReview> review = pageReviewTypeSubtypeService.findRandomPageReview(options2);

        verify(pageIndexHelper, times(1)).indexPageReplacements(any(IndexablePage.class), anyList());

        assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomPageToReviewTypeFiltered() throws ReplacerException {
        // 1 result in DB
        when(
            replacementService.findRandomPageIdsToBeReviewedBySubtype(
                any(WikipediaLanguage.class),
                anyString(),
                anyString(),
                any(PageRequest.class)
            )
        )
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)));

        // The page exists in Wikipedia
        when(wikipediaService.getPageById(WikipediaLanguage.SPANISH, randomId)).thenReturn(Optional.of(page));

        // The page contains replacements
        when(replacementFinderService.find(pageReviewTypeSubtypeService.convertToFinderPage(page)))
            .thenReturn(replacements);

        Optional<PageReview> review = pageReviewTypeSubtypeService.findRandomPageReview(options);

        verify(pageIndexHelper, times(1)).indexPageReplacements(any(IndexablePage.class), anyList());

        assertTrue(review.isPresent());
        assertEquals(randomId, review.get().getPage().getId());
    }

    @Test
    void testFindRandomPageToReviewNoTypeAndThenFiltered() throws ReplacerException {
        // 1. Find the random page 1 by type. In DB there exists also the page 2.
        // 2. Find the random page 2 by no type. The page 2 is supposed to be removed from all the caches.
        // 3. Find a random page by type. In DB there is no page.

        // 2 results in DB by type, no results the second time.
        when(
            replacementService.findRandomPageIdsToBeReviewedBySubtype(
                any(WikipediaLanguage.class),
                anyString(),
                anyString(),
                any(PageRequest.class)
            )
        )
            .thenReturn(new ArrayList<>(Arrays.asList(randomId, randomId2)))
            .thenReturn(Collections.emptyList());
        // 1 result in DB by no type
        when(
            replacementService.findPageIdsToBeReviewed(any(WikipediaLanguage.class), anyLong(), any(PageRequest.class))
        )
            .thenReturn(new ArrayList<>(Collections.singletonList(randomId2)));

        // The pages exist in Wikipedia
        when(wikipediaService.getPageById(WikipediaLanguage.SPANISH, randomId)).thenReturn(Optional.of(page));
        when(wikipediaService.getPageById(WikipediaLanguage.SPANISH, randomId2)).thenReturn(Optional.of(page2));

        // The pages contains replacements
        when(replacementFinderService.find(pageReviewTypeSubtypeService.convertToFinderPage(page)))
            .thenReturn(replacements);
        when(replacementFinderService.find(pageReviewTypeSubtypeService.convertToFinderPage(page2)))
            .thenReturn(replacements);

        Optional<PageReview> review = pageReviewTypeSubtypeService.findRandomPageReview(options);
        assertTrue(review.isPresent());
        assertEquals(randomId, review.get().getPage().getId());

        review = pageReviewTypeSubtypeService.findRandomPageReview(options);
        assertTrue(review.isPresent());
        assertEquals(randomId2, review.get().getPage().getId());

        review = pageReviewTypeSubtypeService.findRandomPageReview(options);
        assertFalse(review.isPresent());
    }

    @Test
    void testPageReviewWithSection() throws ReplacerException {
        final int sectionId = 1;

        // The page exists in Wikipedia
        when(wikipediaService.getPageById(WikipediaLanguage.SPANISH, randomId)).thenReturn(Optional.of(page));

        // The page contains replacements
        when(replacementFinderService.find(pageReviewTypeSubtypeService.convertToFinderPage(page)))
            .thenReturn(replacements);

        // Load the cache in order to find the total results
        pageReviewTypeSubtypeService.loadCache(options);

        // The page has sections
        PageReview sectionReview = pageReviewTypeSubtypeService.buildPageReview(page, replacements, options);
        sectionReview.getPage().setSection(PageSection.of(sectionId, ""));
        when(sectionReviewService.findSectionReview(any(PageReview.class))).thenReturn(Optional.of(sectionReview));

        Optional<PageReview> review = pageReviewTypeSubtypeService.getPageReview(randomId, options);

        assertTrue(review.isPresent());
        review.ifPresent(
            rev -> {
                assertEquals(randomId, rev.getPage().getId());
                assertEquals(replacements.size(), rev.getReplacements().size());
                assertNotNull(rev.getPage().getSection());
                assertNotNull(rev.getPage().getSection().getId());
                assertEquals(sectionId, rev.getPage().getSection().getId().intValue());
            }
        );
    }

    @Test
    void testPageReviewWithNoSection() throws ReplacerException {
        // The page exists in Wikipedia
        when(wikipediaService.getPageById(WikipediaLanguage.SPANISH, randomId)).thenReturn(Optional.of(page));

        // The page contains replacements
        when(replacementFinderService.find(pageReviewTypeSubtypeService.convertToFinderPage(page)))
            .thenReturn(replacements);

        // The page has no sections
        when(sectionReviewService.findSectionReview(any(PageReview.class))).thenReturn(Optional.empty());

        // Load the cache in order to find the total results
        pageReviewTypeSubtypeService.loadCache(options);

        Optional<PageReview> review = pageReviewTypeSubtypeService.getPageReview(randomId, options);

        assertTrue(review.isPresent());
        review.ifPresent(
            rev -> {
                assertEquals(randomId, rev.getPage().getId());
                assertEquals(replacements.size(), rev.getReplacements().size());
                assertNull(rev.getPage().getSection());
            }
        );
    }
}
