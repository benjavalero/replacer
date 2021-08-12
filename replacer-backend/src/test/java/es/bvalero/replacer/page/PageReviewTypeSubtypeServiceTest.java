package es.bvalero.replacer.page;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.replacement.IndexablePage;
import es.bvalero.replacer.replacement.IndexablePageValidator;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
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
        .type("X")
        .subtype("Y")
        .text("Y")
        .build();
    private final List<Replacement> replacements = Collections.singletonList(replacement);
    private final PageReviewOptions options = PageReviewOptions.ofTypeSubtype("X", "Y");
    private final PageReviewOptions options2 = PageReviewOptions.ofTypeSubtype("A", "B");

    @Mock
    private ReplacementService replacementService;

    @Mock
    private WikipediaService wikipediaService;

    @Mock
    private ReplacementFinderService replacementFinderService;

    @Mock
    private ReplacementIndexService replacementIndexService;

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
        Mockito
            .when(
                replacementService.findRandomPageIdsToBeReviewedBySubtype(
                    Mockito.any(WikipediaLanguage.class),
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.any(PageRequest.class)
                )
            )
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)))
            .thenReturn(Collections.emptyList());

        // The page exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(WikipediaLanguage.SPANISH, randomId)).thenReturn(Optional.of(page));

        // The page contains replacements
        Mockito
            .when(replacementFinderService.findList(pageReviewTypeSubtypeService.convertToFinderPage(page)))
            .thenReturn(replacements);

        Optional<PageReview> review = pageReviewTypeSubtypeService.findRandomPageReview(options2);

        Mockito
            .verify(replacementIndexService, Mockito.times(1))
            .indexPageReplacements(Mockito.any(IndexablePage.class), Mockito.anyList());

        Assertions.assertFalse(review.isPresent());
    }

    @Test
    void testFindRandomPageToReviewTypeFiltered() throws ReplacerException {
        // 1 result in DB
        Mockito
            .when(
                replacementService.findRandomPageIdsToBeReviewedBySubtype(
                    Mockito.any(WikipediaLanguage.class),
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.any(PageRequest.class)
                )
            )
            .thenReturn(new ArrayList<>(Collections.singleton(randomId)));

        // The page exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(WikipediaLanguage.SPANISH, randomId)).thenReturn(Optional.of(page));

        // The page contains replacements
        Mockito
            .when(replacementFinderService.findList(pageReviewTypeSubtypeService.convertToFinderPage(page)))
            .thenReturn(replacements);

        Optional<PageReview> review = pageReviewTypeSubtypeService.findRandomPageReview(options);

        Mockito
            .verify(replacementIndexService, Mockito.times(1))
            .indexPageReplacements(Mockito.any(IndexablePage.class), Mockito.anyList());

        Assertions.assertTrue(review.isPresent());
        Assertions.assertEquals(randomId, review.get().getPage().getId());
    }

    @Test
    void testFindRandomPageToReviewNoTypeAndThenFiltered() throws ReplacerException {
        // 1. Find the random page 1 by type. In DB there exists also the page 2.
        // 2. Find the random page 2 by no type. The page 2 is supposed to be removed from all the caches.
        // 3. Find a random page by type. In DB there is no page.

        // 2 results in DB by type, no results the second time.
        Mockito
            .when(
                replacementService.findRandomPageIdsToBeReviewedBySubtype(
                    Mockito.any(WikipediaLanguage.class),
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.any(PageRequest.class)
                )
            )
            .thenReturn(new ArrayList<>(Arrays.asList(randomId, randomId2)))
            .thenReturn(Collections.emptyList());
        // 1 result in DB by no type
        Mockito
            .when(
                replacementService.findPageIdsToBeReviewed(
                    Mockito.any(WikipediaLanguage.class),
                    Mockito.anyLong(),
                    Mockito.any(PageRequest.class)
                )
            )
            .thenReturn(new ArrayList<>(Collections.singletonList(randomId2)));

        // The pages exist in Wikipedia
        Mockito.when(wikipediaService.getPageById(WikipediaLanguage.SPANISH, randomId)).thenReturn(Optional.of(page));
        Mockito.when(wikipediaService.getPageById(WikipediaLanguage.SPANISH, randomId2)).thenReturn(Optional.of(page2));

        // The pages contains replacements
        Mockito
            .when(replacementFinderService.findList(pageReviewTypeSubtypeService.convertToFinderPage(page)))
            .thenReturn(replacements);
        Mockito
            .when(replacementFinderService.findList(pageReviewTypeSubtypeService.convertToFinderPage(page2)))
            .thenReturn(replacements);

        Optional<PageReview> review = pageReviewTypeSubtypeService.findRandomPageReview(options);
        Assertions.assertTrue(review.isPresent());
        Assertions.assertEquals(randomId, review.get().getPage().getId());

        review = pageReviewTypeSubtypeService.findRandomPageReview(options);
        Assertions.assertTrue(review.isPresent());
        Assertions.assertEquals(randomId2, review.get().getPage().getId());

        review = pageReviewTypeSubtypeService.findRandomPageReview(options);
        Assertions.assertFalse(review.isPresent());
    }

    @Test
    void testPageReviewWithSection() throws ReplacerException {
        final int sectionId = 1;

        // The page exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(WikipediaLanguage.SPANISH, randomId)).thenReturn(Optional.of(page));

        // The page contains replacements
        Mockito
            .when(replacementFinderService.findList(pageReviewTypeSubtypeService.convertToFinderPage(page)))
            .thenReturn(replacements);

        // Load the cache in order to find the total results
        pageReviewTypeSubtypeService.loadCache(options);

        // The page has sections
        PageReview sectionReview = pageReviewTypeSubtypeService.buildPageReview(page, replacements, options);
        sectionReview.getPage().setSection(PageSection.of(sectionId, ""));
        Mockito
            .when(sectionReviewService.findSectionReview(Mockito.any(PageReview.class)))
            .thenReturn(Optional.of(sectionReview));

        Optional<PageReview> review = pageReviewTypeSubtypeService.getPageReview(randomId, options);

        Assertions.assertTrue(review.isPresent());
        review.ifPresent(
            rev -> {
                Assertions.assertEquals(randomId, rev.getPage().getId());
                Assertions.assertEquals(replacements.size(), rev.getReplacements().size());
                Assertions.assertNotNull(rev.getPage().getSection());
                Assertions.assertNotNull(rev.getPage().getSection().getId());
                Assertions.assertEquals(sectionId, rev.getPage().getSection().getId().intValue());
            }
        );
    }

    @Test
    void testPageReviewWithNoSection() throws ReplacerException {
        // The page exists in Wikipedia
        Mockito.when(wikipediaService.getPageById(WikipediaLanguage.SPANISH, randomId)).thenReturn(Optional.of(page));

        // The page contains replacements
        Mockito
            .when(replacementFinderService.findList(pageReviewTypeSubtypeService.convertToFinderPage(page)))
            .thenReturn(replacements);

        // The page has no sections
        Mockito
            .when(sectionReviewService.findSectionReview(Mockito.any(PageReview.class)))
            .thenReturn(Optional.empty());

        // Load the cache in order to find the total results
        pageReviewTypeSubtypeService.loadCache(options);

        Optional<PageReview> review = pageReviewTypeSubtypeService.getPageReview(randomId, options);

        Assertions.assertTrue(review.isPresent());
        review.ifPresent(
            rev -> {
                Assertions.assertEquals(randomId, rev.getPage().getId());
                Assertions.assertEquals(replacements.size(), rev.getReplacements().size());
                Assertions.assertNull(rev.getPage().getSection());
            }
        );
    }
}
