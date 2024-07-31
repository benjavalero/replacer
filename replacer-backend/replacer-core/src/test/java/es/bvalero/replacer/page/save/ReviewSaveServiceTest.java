package es.bvalero.replacer.page.save;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageRepository;
import es.bvalero.replacer.page.find.WikipediaTimestamp;
import es.bvalero.replacer.replacement.CustomReplacementService;
import es.bvalero.replacer.replacement.IndexedCustomReplacement;
import es.bvalero.replacer.replacement.ReplacementSaveRepository;
import es.bvalero.replacer.user.User;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewSaveServiceTest {

    private static final int MAX_EDITIONS_PER_MINUTE = 5;

    // Dependency injection
    private PageRepository pageRepository;
    private ReplacementSaveRepository replacementSaveRepository;
    private CustomReplacementService customReplacementService;
    private WikipediaPageSaveRepository wikipediaPageSaveRepository;

    private ReviewSaveService reviewSaveService;

    @BeforeEach
    public void setUp() {
        pageRepository = mock(PageRepository.class);
        replacementSaveRepository = mock(ReplacementSaveRepository.class);
        customReplacementService = mock(CustomReplacementService.class);
        wikipediaPageSaveRepository = mock(WikipediaPageSaveRepository.class);
        reviewSaveService = new ReviewSaveService(
            pageRepository,
            replacementSaveRepository,
            customReplacementService,
            wikipediaPageSaveRepository
        );
        reviewSaveService.setMaxEditionsPerMinute(MAX_EDITIONS_PER_MINUTE);
    }

    @Test
    void testSaveWithChanges() throws WikipediaException {
        int id = 123;
        PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), id);
        WikipediaPageSaveCommand pageSave = WikipediaPageSaveCommand.builder()
            .pageKey(pageKey)
            .content("X")
            .editSummary("S")
            .queryTimestamp(WikipediaTimestamp.now())
            .build();
        User user = User.buildTestUser();
        reviewSaveService.saveReviewContent(pageSave, user);

        verify(wikipediaPageSaveRepository).save(pageSave, user.getAccessToken());
    }

    @Test
    void testMarkAsReviewed() {
        int pageId = 123;
        PageKey pageKey = PageKey.of(WikipediaLanguage.getDefault(), pageId);
        String reviewer = "X";

        ReviewedReplacement r1 = ReviewedReplacement.builder()
            .pageKey(pageKey)
            .type(StandardType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .reviewer(reviewer)
            .build();
        ReviewedReplacement r2 = ReviewedReplacement.builder()
            .pageKey(pageKey)
            .type(StandardType.of(ReplacementKind.COMPOSED, "2"))
            .start(2)
            .reviewer(reviewer)
            .build();
        ReviewedReplacement r3 = ReviewedReplacement.builder()
            .pageKey(pageKey)
            .type(CustomType.of("3", false))
            .start(3)
            .reviewer(reviewer)
            .build();
        List<ReviewedReplacement> reviewedReplacements = List.of(r1, r2, r3);

        LocalDateTime now = LocalDateTime.now();
        reviewSaveService.markAsReviewed(reviewedReplacements, WikipediaPageSaveResult.ofDummy());

        verify(pageRepository).updateLastUpdate(pageKey, now.toLocalDate());
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
        WikipediaPageSaveCommand pageSave = WikipediaPageSaveCommand.builder()
            .pageKey(pageKey)
            .content("X")
            .editSummary("S")
            .queryTimestamp(WikipediaTimestamp.now())
            .build();
        User user = User.buildTestUser();

        for (int i = 0; i < MAX_EDITIONS_PER_MINUTE; i++) {
            reviewSaveService.saveReviewContent(pageSave, user);
        }

        verify(wikipediaPageSaveRepository, times(MAX_EDITIONS_PER_MINUTE)).save(pageSave, user.getAccessToken());

        assertThrows(WikipediaException.class, () -> reviewSaveService.saveReviewContent(pageSave, user));
    }
}
