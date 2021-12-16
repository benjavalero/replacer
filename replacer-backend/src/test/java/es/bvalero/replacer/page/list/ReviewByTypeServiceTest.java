package es.bvalero.replacer.page.list;

import static es.bvalero.replacer.repository.ReplacementRepository.REVIEWER_SYSTEM;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.ReplacementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReviewByTypeServiceTest {

    @Mock
    private ReplacementRepository replacementRepository;

    @InjectMocks
    private ReviewByTypeService reviewByTypeService;

    @BeforeEach
    public void setUp() {
        reviewByTypeService = new ReviewByTypeService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testReviewAsSystemByType() {
        reviewByTypeService.reviewAsSystemByType(
            WikipediaLanguage.getDefault(),
            ReplacementType.of(ReplacementKind.DATE, "Y")
        );

        verify(replacementRepository)
            .updateReviewerByType(
                WikipediaLanguage.getDefault(),
                ReplacementKind.DATE.getLabel(),
                "Y",
                REVIEWER_SYSTEM
            );
    }
}