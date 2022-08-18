package es.bvalero.replacer.review.save;

import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.repository.*;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReviewSaveServiceTest {

    @Mock
    private PageRepository pageRepository;

    @Mock
    private ReplacementTypeRepository replacementTypeRepository;

    @Mock
    private PageIndexRepository pageIndexRepository;

    @Mock
    private CustomRepository customRepository;

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
        WikipediaPageId pageId = WikipediaPageId.of(WikipediaLanguage.getDefault(), id);
        LocalDateTime timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        WikipediaPage page = WikipediaPage
            .builder()
            .id(pageId)
            .namespace(WikipediaNamespace.getDefault()) // Not relevant for saving
            .title("T")
            .content("X")
            .lastUpdate(timestamp)
            .queryTimestamp(timestamp)
            .build();
        ReviewedReplacement reviewed = ReviewedReplacement
            .builder()
            .pageId(pageId)
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
        verify(wikipediaPageRepository)
            .save(eq(page.getId()), isNull(), eq(contentAfterCosmetics), eq(timestamp), anyString(), eq(accessToken));
    }

    @Test
    void testMarkAsReviewed() {
        int pageId = 123;
        WikipediaPageId wikipediaPageId = WikipediaPageId.of(WikipediaLanguage.getDefault(), pageId);
        String reviewer = "X";

        ReviewedReplacement r1 = ReviewedReplacement
            .builder()
            .pageId(wikipediaPageId)
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "1"))
            .start(1)
            .reviewer(reviewer)
            .build();
        ReviewedReplacement r2 = ReviewedReplacement
            .builder()
            .pageId(wikipediaPageId)
            .type(ReplacementType.of(ReplacementKind.COMPOSED, "2"))
            .start(2)
            .reviewer(reviewer)
            .build();
        ReviewedReplacement r3 = ReviewedReplacement
            .builder()
            .pageId(wikipediaPageId)
            .type(ReplacementType.of(ReplacementKind.CUSTOM, "3"))
            .start(3)
            .cs(false)
            .reviewer(reviewer)
            .build();
        List<ReviewedReplacement> reviewedReplacements = List.of(r1, r2, r3);

        reviewSaveService.markAsReviewed(reviewedReplacements, true);

        verify(pageRepository).updatePageLastUpdate(wikipediaPageId, LocalDate.now());
        verify(replacementTypeRepository, times(2)).updateReviewer(any(ReplacementModel.class));
        verify(customRepository, times(1)).addCustom(any(CustomModel.class));
    }
}
