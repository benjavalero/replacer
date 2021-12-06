package es.bvalero.replacer.replacement.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReplacementStatsServiceTest {

    @Mock
    private ReplacementStatsRepository replacementStatsRepository;

    @InjectMocks
    private ReplacementStatsService replacementDaoProxy;

    @BeforeEach
    public void setUp() {
        replacementDaoProxy = new ReplacementStatsService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testCountReplacementsReviewed() {
        long count = new Random().nextLong();

        when(replacementStatsRepository.countReplacementsReviewed(any(WikipediaLanguage.class))).thenReturn(count);

        assertEquals(count, replacementDaoProxy.countReplacementsReviewed(WikipediaLanguage.SPANISH));
    }

    @Test
    void testCountReplacementsToReview() {
        long count = new Random().nextLong();

        when(replacementStatsRepository.countReplacementsNotReviewed(any(WikipediaLanguage.class))).thenReturn(count);

        assertEquals(count, replacementDaoProxy.countReplacementsNotReviewed(WikipediaLanguage.SPANISH));
    }

    @Test
    void testCountReplacementsGroupedByReviewer() {
        List<ReviewerCount> result = new ArrayList<>();

        when(replacementStatsRepository.countReplacementsGroupedByReviewer(any(WikipediaLanguage.class)))
            .thenReturn(result);

        assertEquals(result, replacementDaoProxy.countReplacementsGroupedByReviewer(WikipediaLanguage.SPANISH));
    }
}
