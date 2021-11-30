package es.bvalero.replacer.page.review;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageReviewSectionFinderTest {

    @Mock
    private WikipediaService wikipediaService;

    @InjectMocks
    private PageReviewSectionFinder pageReviewSectionFinder;

    @BeforeEach
    public void setUp() {
        pageReviewSectionFinder = new PageReviewSectionFinder();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindSectionReviewNoSections() throws ReplacerException {
        WikipediaPage page = mock(WikipediaPage.class);
        Collection<Replacement> replacements = Collections.emptyList();

        when(wikipediaService.getPageSections(any(WikipediaPageId.class))).thenReturn(Collections.emptyList());

        PageReview review = PageReview
            .builder()
            .page(page)
            .replacements(Collections.emptyList())
            .numPending(1L)
            .build();
        Optional<PageReview> sectionReview = pageReviewSectionFinder.findPageReviewSection(review, page, replacements);

        assertFalse(sectionReview.isPresent());
    }

    @Test
    void testFindSectionReview() throws ReplacerException {
        // TODO: Check if all of this is needed
        int pageId = 1;
        String content = "This is an sample content.";
        Suggestion suggestion = Suggestion.ofNoComment("a");
        Replacement replacement = Replacement
            .builder()
            .start(8)
            .text("an")
            .type(ReplacementType.MISSPELLING_SIMPLE)
            .subtype("an")
            .suggestions(Collections.singletonList(suggestion))
            .build();
        List<Replacement> replacements = Collections.singletonList(replacement);
        WikipediaPage page = WikipediaPage
            .builder()
            .id(WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId))
            .namespace(WikipediaNamespace.getDefault())
            .title("T")
            .content(content)
            .lastUpdate(LocalDateTime.now())
            .build();
        PageReview pageReview = PageReview.builder().page(page).replacements(replacements).numPending(1L).build();

        Integer sectionId = 3;
        int offset = 5;
        WikipediaSection section = WikipediaSection
            .builder()
            .level(2)
            .index(sectionId)
            .byteOffset(offset)
            .anchor("X")
            .build();
        when(wikipediaService.getPageSections(any(WikipediaPageId.class)))
            .thenReturn(Collections.singletonList(section));

        String sectionContent = content.substring(offset, 10);
        WikipediaPageId wikipediaPageId = WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId);
        WikipediaPage pageSection = WikipediaPage
            .builder()
            .id(wikipediaPageId)
            .namespace(WikipediaNamespace.getDefault())
            .title("Title")
            .content(sectionContent)
            .lastUpdate(LocalDateTime.now())
            .build();
        when(wikipediaService.getPageSection(wikipediaPageId, section)).thenReturn(Optional.of(pageSection));

        Optional<PageReview> sectionReview = pageReviewSectionFinder.findPageReviewSection(
            pageReview,
            page,
            replacements
        );

        assertTrue(sectionReview.isPresent());
        sectionReview.ifPresent(
            review -> {
                assertEquals(pageId, review.getPage().getId().getPageId());
                assertNotNull(review.getSection());
                assertNotNull(review.getSection().getIndex());
                assertEquals(sectionId, review.getSection().getIndex());
                assertEquals(sectionContent, review.getPage().getContent());
                assertEquals(1, review.getReplacements().size());
                assertEquals(8 - offset, review.getReplacements().get(0).getStart());
            }
        );
    }
}
