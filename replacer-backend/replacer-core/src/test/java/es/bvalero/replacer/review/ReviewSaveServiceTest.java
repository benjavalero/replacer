package es.bvalero.replacer.review;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageService;
import es.bvalero.replacer.replacement.CustomReplacementService;
import es.bvalero.replacer.replacement.IndexedCustomReplacement;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.wikipedia.*;
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

    @Mock
    private ApplyCosmeticsService applyCosmeticsService;

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
        WikipediaPage page = WikipediaPage
            .builder()
            .pageKey(pageKey)
            .namespace(WikipediaNamespace.getDefault()) // Not relevant for saving
            .title("T")
            .content("X")
            .lastUpdate(WikipediaTimestamp.now())
            .queryTimestamp(WikipediaTimestamp.now())
            .build();
        ReviewedReplacement reviewed = ReviewedReplacement
            .builder()
            .pageKey(pageKey)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .reviewer("A")
            .fixed(true)
            .build();

        String contentAfterCosmetics = "C";
        when(applyCosmeticsService.applyCosmeticChanges(page)).thenReturn(contentAfterCosmetics);

        AccessToken accessToken = AccessToken.of("A", "B");
        reviewSaveService.saveReviewContent(page, null, List.of(reviewed), accessToken);

        verify(applyCosmeticsService).applyCosmeticChanges(page);
        verify(wikipediaPageRepository).save(any(WikipediaPageSave.class), eq(accessToken));
    }

    @Test
    void testMarkAsReviewed() {
        int pageId = 123;
        PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), pageId);
        String reviewer = "X";

        ReviewedReplacement r1 = ReviewedReplacement
            .builder()
            .pageKey(pageKey)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .reviewer(reviewer)
            .build();
        ReviewedReplacement r2 = ReviewedReplacement
            .builder()
            .pageKey(pageKey)
            .type(ReplacementType.of(ReplacementKind.COMPOSED, "2"))
            .start(2)
            .reviewer(reviewer)
            .build();
        ReviewedReplacement r3 = ReviewedReplacement
            .builder()
            .pageKey(pageKey)
            .type(ReplacementType.of(ReplacementKind.CUSTOM, "3"))
            .start(3)
            .cs(false)
            .reviewer(reviewer)
            .build();
        List<ReviewedReplacement> reviewedReplacements = List.of(r1, r2, r3);

        reviewSaveService.markAsReviewed(reviewedReplacements, true);

        verify(pageService).updatePageLastUpdate(pageKey, LocalDate.now());
        verify(replacementService, times(1)).updateReviewer(anyCollection());
        verify(customReplacementService, times(1)).addCustomReplacement(any(IndexedCustomReplacement.class));
    }

    @Test
    void testBuildEditSummary() {
        int pageId = 123;
        PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), pageId);
        String reviewer = "X";

        ReviewedReplacement r1 = ReviewedReplacement
            .builder()
            .pageKey(pageKey)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .reviewer(reviewer)
            .fixed(true)
            .build();
        ReviewedReplacement r2 = ReviewedReplacement
            .builder()
            .pageKey(pageKey)
            .type(ReplacementType.of(ReplacementKind.COMPOSED, "2"))
            .start(2)
            .reviewer(reviewer)
            .fixed(true)
            .build();
        ReviewedReplacement r3 = ReviewedReplacement
            .builder()
            .pageKey(pageKey)
            .type(ReplacementType.of(ReplacementKind.CUSTOM, "3"))
            .start(3)
            .cs(false)
            .reviewer(reviewer)
            .fixed(true)
            .build();
        ReviewedReplacement r4 = ReviewedReplacement
            .builder()
            .pageKey(pageKey)
            .type(ReplacementType.DATE)
            .start(4)
            .reviewer(reviewer)
            .fixed(true)
            .build();
        List<ReviewedReplacement> reviewedReplacements = List.of(r1, r2, r3, r4);

        String summary = reviewSaveService.buildEditSummary(reviewedReplacements, false);
        assertTrue(summary.contains("«1»"));
        assertTrue(summary.contains("«2»"));
        assertTrue(summary.contains("«3»"));
        assertFalse(summary.contains("«Fechas»"));
        assertTrue(summary.contains("Fechas"));
    }
}
