package es.bvalero.replacer.page.list;

import static es.bvalero.replacer.repository.ReplacementRepository.REVIEWER_SYSTEM;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReviewByTypeServiceTest {

    @Mock
    private ReplacementTypeRepository replacementTypeRepository;

    @InjectMocks
    private ReviewByTypeService reviewByTypeService;

    @BeforeEach
    public void setUp() {
        reviewByTypeService = new ReviewByTypeService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testReviewAsSystemByType() {
        ReplacementType type = ReplacementType.of(ReplacementKind.DATE, "Y");

        reviewByTypeService.reviewAsSystemByType(WikipediaLanguage.getDefault(), type);

        verify(replacementTypeRepository).updateReviewerByType(WikipediaLanguage.getDefault(), type, REVIEWER_SYSTEM);
    }
}
