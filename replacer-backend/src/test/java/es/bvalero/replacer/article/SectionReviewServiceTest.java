package es.bvalero.replacer.article;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.wikipedia.*;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;

class SectionReviewServiceTest {
    @Mock
    private WikipediaService wikipediaService;

    @Spy
    private ModelMapper modelMapper;

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

        ArticleReview review = new ArticleReview();
        Optional<ArticleReview> sectionReview = sectionReviewService.findSectionReview(review);

        Assertions.assertFalse(sectionReview.isPresent());
    }

    @Test
    void testFindSectionReview() throws ReplacerException {
        int articleId = 1;
        String content = "This is an sample content.";
        Suggestion suggestion = Suggestion.ofNoComment("a");
        ArticleReplacement replacement = new ArticleReplacement(8, "an", Collections.singletonList(suggestion)); // "an"

        ArticleReview articleReview = new ArticleReview();
        articleReview.setId(articleId);
        articleReview.setLang(WikipediaLanguage.SPANISH);
        articleReview.setContent(content);
        articleReview.setReplacements(Collections.singletonList(replacement));

        int sectionId = 3;
        int offset = 5;
        WikipediaSection section = WikipediaSection.builder().index(sectionId).level(2).byteOffset(offset).build();
        Mockito
            .when(wikipediaService.getPageSections(Mockito.anyInt(), Mockito.any(WikipediaLanguage.class)))
            .thenReturn(Collections.singletonList(section));

        String sectionContent = content.substring(offset, 10);
        WikipediaPage pageSection = WikipediaPage
            .builder()
            .id(articleId)
            .lang(WikipediaLanguage.SPANISH)
            .content(sectionContent)
            .section(sectionId)
            .build();
        Mockito
            .when(
                wikipediaService.getPageByIdAndSection(
                    Mockito.eq(articleId),
                    Mockito.eq(sectionId),
                    Mockito.any(WikipediaLanguage.class)
                )
            )
            .thenReturn(Optional.of(pageSection));

        Optional<ArticleReview> sectionReview = sectionReviewService.findSectionReview(articleReview);

        Assertions.assertTrue(sectionReview.isPresent());
        sectionReview.ifPresent(
            review -> {
                Assertions.assertEquals(articleId, review.getId());
                Assertions.assertEquals(Integer.valueOf(sectionId), review.getSection());
                Assertions.assertEquals(sectionContent, review.getContent());
                Assertions.assertEquals(1, review.getReplacements().size());
                Assertions.assertEquals(8 - offset, review.getReplacements().get(0).getStart());
            }
        );
    }
}
