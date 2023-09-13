package es.bvalero.replacer.page.review;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageService;
import es.bvalero.replacer.replacement.CustomReplacementService;
import es.bvalero.replacer.replacement.IndexedCustomReplacement;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import es.bvalero.replacer.wikipedia.WikipediaPageSave;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReviewSaveServiceTest {

    @Mock
    private PageService pageService;

    @Mock
    private ReplacementService replacementService;

    @Mock
    private CustomReplacementService customReplacementService;

    @Mock
    private WikipediaPageRepository wikipediaPageRepository;

    @InjectMocks
    private ReviewSaveService reviewSaveService;

    @BeforeEach
    public void setUp() {
        reviewSaveService = new ReviewSaveService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveWithChanges() throws WikipediaException {
        int id = 123;
        PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), id);
        WikipediaPageSave pageSave = WikipediaPageSave
            .builder()
            .pageKey(pageKey)
            .content("X")
            .editSummary("S")
            .queryTimestamp(WikipediaTimestamp.now())
            .build();
        AccessToken accessToken = AccessToken.of("A", "B");
        reviewSaveService.saveReviewContent(pageSave, accessToken);

        verify(wikipediaPageRepository).save(pageSave, accessToken);
    }

    @Test
    void testMarkAsReviewed() {
        int pageId = 123;
        PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), pageId);
        String reviewer = "X";

        ReviewedReplacement r1 = ReviewedReplacement
            .builder()
            .pageKey(pageKey)
            .type(StandardType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .reviewer(reviewer)
            .build();
        ReviewedReplacement r2 = ReviewedReplacement
            .builder()
            .pageKey(pageKey)
            .type(StandardType.of(ReplacementKind.COMPOSED, "2"))
            .start(2)
            .reviewer(reviewer)
            .build();
        ReviewedReplacement r3 = ReviewedReplacement
            .builder()
            .pageKey(pageKey)
            .type(CustomType.of("3", false))
            .start(3)
            .reviewer(reviewer)
            .build();
        List<ReviewedReplacement> reviewedReplacements = List.of(r1, r2, r3);

        reviewSaveService.markAsReviewed(reviewedReplacements, true);

        verify(pageService).updatePageLastUpdate(pageKey, LocalDate.now());
        verify(replacementService).updateReviewer(anyCollection());
        verify(customReplacementService).addCustomReplacement(any(IndexedCustomReplacement.class));
    }

    @Test
    void testBuildEditSummary() {
        ReplacementType r1 = StandardType.of(ReplacementKind.SIMPLE, "1");
        ReplacementType r2 = StandardType.of(ReplacementKind.COMPOSED, "2");
        ReplacementType r3 = CustomType.of("3", false);
        ReplacementType r4 = StandardType.DATE;
        List<ReplacementType> fixedReplacementTypes = List.of(r1, r2, r3, r4);

        String summary = reviewSaveService.buildEditSummary(fixedReplacementTypes, false);
        assertTrue(summary.contains("«1»"));
        assertTrue(summary.contains("«2»"));
        assertTrue(summary.contains("«3»"));
        assertFalse(summary.contains("«Fechas»"));
        assertTrue(summary.contains("Fechas"));
    }
}
