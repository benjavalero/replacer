package es.bvalero.replacer.page.review;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDateTime;
import java.util.*;
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
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindSectionReviewNoSections() throws WikipediaException {
        WikipediaPage page = mock(WikipediaPage.class);
        List<PageReplacement> replacements = Collections.emptyList();

        when(wikipediaService.getPageSections(any(WikipediaPageId.class))).thenReturn(Collections.emptyList());

        PageReview review = PageReview.of(page, null, replacements, 1L);
        Optional<PageReview> sectionReview = pageReviewSectionFinder.findPageReviewSection(review);

        assertFalse(sectionReview.isPresent());
    }

    @Test
    void testFindSectionReview() throws WikipediaException {
        int pageId = 1;
        String content = "This is an sample content.";
        Suggestion suggestion = Suggestion.ofNoComment("a");
        PageReplacement replacement = PageReplacement
            .builder()
            .start(8)
            .text("an")
            .type(ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "an"))
            .suggestions(Collections.singletonList(suggestion))
            .build();
        List<PageReplacement> replacements = Collections.singletonList(replacement);
        WikipediaPage page = WikipediaPage
            .builder()
            .id(WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId))
            .namespace(WikipediaNamespace.getDefault())
            .title("T")
            .content(content)
            .lastUpdate(LocalDateTime.now())
            .build();
        Long numPending = 10L;
        PageReview pageReview = PageReview.of(page, null, replacements, numPending);

        Integer sectionId = 3;
        int offset = 5;
        WikipediaSection section = WikipediaSection
            .builder()
            .level(2)
            .index(sectionId)
            .byteOffset(offset)
            .anchor("X")
            .build();
        when(wikipediaService.getPageSections(page.getId())).thenReturn(Collections.singletonList(section));

        String sectionContent = content.substring(offset, 10);
        WikipediaPage pageSection = WikipediaPage
            .builder()
            .id(page.getId())
            .namespace(page.getNamespace())
            .title(page.getTitle())
            .content(sectionContent)
            .lastUpdate(page.getLastUpdate())
            .build();
        when(wikipediaService.getPageSection(page.getId(), section)).thenReturn(Optional.of(pageSection));

        Optional<PageReview> sectionReview = pageReviewSectionFinder.findPageReviewSection(pageReview);

        assertTrue(sectionReview.isPresent());
        sectionReview.ifPresent(review -> {
            assertEquals(page.getId(), review.getPage().getId());
            assertNotNull(review.getSection());
            assertNotNull(review.getSection().getIndex());
            assertEquals(section, review.getSection());
            assertEquals(sectionContent, review.getPage().getContent());
            assertEquals(1, review.getReplacements().size());
            assertEquals(8 - offset, new ArrayList<>(review.getReplacements()).get(0).getStart());
            assertEquals(numPending, review.getNumPending());
        });
    }
}
