package es.bvalero.replacer.page;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.replacement.Suggestion;
import es.bvalero.replacer.wikipedia.*;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

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
        Mockito
            .when(wikipediaService.getPageSections(Mockito.anyInt(), Mockito.any(WikipediaLanguage.class)))
            .thenReturn(Collections.emptyList());

        PageReview review = PageReview.ofEmpty();
        Optional<PageReview> sectionReview = sectionReviewService.findSectionReview(review);

        Assertions.assertFalse(sectionReview.isPresent());
    }

    @Test
    void testFindSectionReview() throws ReplacerException {
        int pageId = 1;
        String content = "This is an sample content.";
        Suggestion suggestion = Suggestion.of("a", "");
        PageReplacement replacement = PageReplacement.of(8, "an", Collections.singletonList(suggestion)); // "an"

        PageReview pageReview = PageReview
            .builder()
            .id(pageId)
            .lang(WikipediaLanguage.SPANISH)
            .content(content)
            .replacements(Collections.singletonList(replacement))
            .build();

        int sectionId = 3;
        int offset = 5;
        WikipediaSection section = WikipediaSection.builder().level(2).index(sectionId).byteOffset(offset).build();
        Mockito
            .when(wikipediaService.getPageSections(Mockito.anyInt(), Mockito.any(WikipediaLanguage.class)))
            .thenReturn(Collections.singletonList(section));

        String sectionContent = content.substring(offset, 10);
        WikipediaPage pageSection = WikipediaPage
            .builder()
            .id(pageId)
            .lang(WikipediaLanguage.SPANISH)
            .content(sectionContent)
            .section(sectionId)
            .build();
        Mockito
            .when(
                wikipediaService.getPageByIdAndSection(
                    Mockito.eq(pageId),
                    Mockito.eq(section),
                    Mockito.any(WikipediaLanguage.class)
                )
            )
            .thenReturn(Optional.of(pageSection));

        Optional<PageReview> sectionReview = sectionReviewService.findSectionReview(pageReview);

        Assertions.assertTrue(sectionReview.isPresent());
        sectionReview.ifPresent(
            review -> {
                Assertions.assertEquals(pageId, review.getId());
                Assertions.assertEquals(Integer.valueOf(sectionId), review.getSection());
                Assertions.assertEquals(sectionContent, review.getContent());
                Assertions.assertEquals(1, review.getReplacements().size());
                Assertions.assertEquals(8 - offset, review.getReplacements().get(0).getStart());
            }
        );
    }
}
