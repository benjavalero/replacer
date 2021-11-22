package es.bvalero.replacer.page;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SectionReviewServiceTest {

    @Mock
    private WikipediaService wikipediaService;

    @InjectMocks
    private SectionReviewService sectionReviewService;

    @BeforeEach
    public void setUp() {
        sectionReviewService = new SectionReviewService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindSectionReviewNoSections() throws ReplacerException {
        when(wikipediaService.getPageSections(any(WikipediaLanguage.class), anyInt()))
            .thenReturn(Collections.emptyList());

        PageReview review = PageReview.ofEmpty();
        Optional<PageReview> sectionReview = sectionReviewService.findSectionReview(review);

        assertFalse(sectionReview.isPresent());
    }

    @Test
    void testFindSectionReview() throws ReplacerException {
        int pageId = 1;
        String content = "This is an sample content.";
        PageReplacementSuggestion suggestion = PageReplacementSuggestion.of("a", "");
        PageReplacement replacement = PageReplacement.of(8, "an", Collections.singletonList(suggestion)); // "an"

        PageDto page = PageDto.builder().id(pageId).lang(WikipediaLanguage.getDefault()).content(content).build();
        PageReview pageReview = PageReview
            .builder()
            .page(page)
            .replacements(Collections.singletonList(replacement))
            .build();

        Integer sectionId = 3;
        int offset = 5;
        WikipediaSection section = WikipediaSection
            .builder()
            .level(2)
            .index(sectionId)
            .byteOffset(offset)
            .anchor("X")
            .build();
        when(wikipediaService.getPageSections(any(WikipediaLanguage.class), anyInt()))
            .thenReturn(Collections.singletonList(section));

        String sectionContent = content.substring(offset, 10);
        WikipediaPageSection pageSection = WikipediaPageSection
            .builder()
            .id(WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId))
            .namespace(WikipediaNamespace.getDefault())
            .title("Title")
            .content(sectionContent)
            .section(section)
            .lastUpdate(LocalDateTime.now())
            .build();
        when(wikipediaService.getPageSection(any(WikipediaLanguage.class), eq(pageId), eq(section)))
            .thenReturn(Optional.of(pageSection));

        Optional<PageReview> sectionReview = sectionReviewService.findSectionReview(pageReview);

        assertTrue(sectionReview.isPresent());
        sectionReview.ifPresent(
            review -> {
                assertEquals(pageId, review.getPage().getId());
                assertNotNull(review.getPage().getSection());
                assertNotNull(review.getPage().getSection().getId());
                assertEquals(sectionId, review.getPage().getSection().getId());
                assertEquals(sectionContent, review.getPage().getContent());
                assertEquals(1, review.getReplacements().size());
                assertEquals(8 - offset, review.getReplacements().get(0).getStart());
            }
        );
    }
}
