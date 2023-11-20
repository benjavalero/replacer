package es.bvalero.replacer.page.save;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageService;
import es.bvalero.replacer.replacement.CustomReplacementService;
import es.bvalero.replacer.replacement.IndexedCustomReplacement;
import es.bvalero.replacer.replacement.ReplacementSaveRepository;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import es.bvalero.replacer.wikipedia.WikipediaPageSave;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewSaveServiceTest {

    private static final int MAX_EDITIONS_PER_MINUTE = 5;

    // Dependency injection
    private PageService pageService;
    private ReplacementSaveRepository replacementSaveRepository;
    private CustomReplacementService customReplacementService;
    private WikipediaPageRepository wikipediaPageRepository;

    private ReviewSaveService reviewSaveService;

    @BeforeEach
    public void setUp() {
        pageService = mock(PageService.class);
        replacementSaveRepository = mock(ReplacementSaveRepository.class);
        customReplacementService = mock(CustomReplacementService.class);
        wikipediaPageRepository = mock(WikipediaPageRepository.class);
        reviewSaveService =
            new ReviewSaveService(
                pageService,
                replacementSaveRepository,
                customReplacementService,
                wikipediaPageRepository
            );
        reviewSaveService.setMaxEditionsPerMinute(MAX_EDITIONS_PER_MINUTE);
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
        User user = User.buildTestUser();
        reviewSaveService.saveReviewContent(pageSave, user);

        verify(wikipediaPageRepository).save(pageSave, user.getAccessToken());
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
        verify(replacementSaveRepository).updateReviewer(anyCollection());
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

    @Test
    void testMaximumEditionsPerMinute() throws WikipediaException {
        int id = 123;
        PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), id);
        WikipediaPageSave pageSave = WikipediaPageSave
            .builder()
            .pageKey(pageKey)
            .content("X")
            .editSummary("S")
            .queryTimestamp(WikipediaTimestamp.now())
            .build();
        User user = User.buildTestUser();

        for (int i = 0; i < MAX_EDITIONS_PER_MINUTE; i++) {
            reviewSaveService.saveReviewContent(pageSave, user);
        }

        verify(wikipediaPageRepository, times(MAX_EDITIONS_PER_MINUTE)).save(pageSave, user.getAccessToken());

        assertThrows(WikipediaException.class, () -> reviewSaveService.saveReviewContent(pageSave, user));
    }
}
