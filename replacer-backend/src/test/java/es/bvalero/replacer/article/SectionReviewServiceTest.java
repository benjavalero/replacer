package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaSection;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.Optional;

public class SectionReviewServiceTest {

    @Mock
    private WikipediaService wikipediaService;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private SectionReviewService sectionReviewService;

    @Before
    public void setUp() {
        sectionReviewService = new SectionReviewService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void findSectionReviewNoSections() throws WikipediaException {
        Mockito.when(wikipediaService.getPageSections(Mockito.anyInt())).thenReturn(Collections.emptyList());

        ArticleReview review = new ArticleReview();
        Optional<ArticleReview> sectionReview = sectionReviewService.findSectionReview(review);

        Assert.assertFalse(sectionReview.isPresent());
    }

    @Test
    public void findSectionReview() throws WikipediaException {
        int articleId = 1;
        String content = "This is an sample content.";
        Suggestion suggestion = Suggestion.ofNoComment("a");
        ArticleReplacement replacement = new ArticleReplacement(8, "an", Collections.singletonList(suggestion)); // "an"

        ArticleReview articleReview = new ArticleReview();
        articleReview.setId(articleId);
        articleReview.setContent(content);
        articleReview.setReplacements(Collections.singletonList(replacement));

        int sectionId = 3;
        int offset = 5;
        WikipediaSection section = WikipediaSection.builder()
                .index(sectionId).level(2).byteOffset(offset)
                .build();
        Mockito.when(wikipediaService.getPageSections(Mockito.anyInt())).thenReturn(Collections.singletonList(section));

        String sectionContent = content.substring(offset, 10);
        WikipediaPage pageSection = WikipediaPage.builder()
                .id(articleId)
                .content(sectionContent)
                .section(sectionId)
                .build();
        Mockito.when(wikipediaService.getPageByIdAndSection(Mockito.eq(articleId), Mockito.eq(sectionId)))
                .thenReturn(Optional.of(pageSection));

        Optional<ArticleReview> sectionReview = sectionReviewService.findSectionReview(articleReview);

        Assert.assertTrue(sectionReview.isPresent());
        sectionReview.ifPresent(review -> {
            Assert.assertEquals(articleId, review.getId());
            Assert.assertEquals(Integer.valueOf(sectionId), review.getSection());
            Assert.assertEquals(sectionContent, review.getContent());
            Assert.assertEquals(1, review.getReplacements().size());
            Assert.assertEquals(8 - offset, review.getReplacements().get(0).getStart());
        });
    }

}
