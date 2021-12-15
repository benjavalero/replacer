package es.bvalero.replacer.replacement.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.ReplacementRepository;
import es.bvalero.replacer.repository.ResultCount;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReplacementStatsServiceTest {

    @Mock
    private ReplacementRepository replacementRepository;

    @InjectMocks
    private ReplacementStatsService replacementStatsService;

    @BeforeEach
    public void setUp() {
        replacementStatsService = new ReplacementStatsService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCountReplacementsReviewed() {
        long count = new Random().nextLong();

        when(replacementRepository.countReplacementsReviewed(any(WikipediaLanguage.class))).thenReturn(count);

        assertEquals(count, replacementStatsService.countReplacementsReviewed(WikipediaLanguage.SPANISH));
    }

    @Test
    void testCountReplacementsToReview() {
        long count = new Random().nextLong();

        when(replacementRepository.countReplacementsNotReviewed(any(WikipediaLanguage.class))).thenReturn(count);

        assertEquals(count, replacementStatsService.countReplacementsNotReviewed(WikipediaLanguage.SPANISH));
    }

    @Test
    void testCountReplacementsGroupedByReviewer() {
        ResultCount<String> count = ResultCount.of("A", 10L);
        when(replacementRepository.countReplacementsByReviewer(any(WikipediaLanguage.class)))
            .thenReturn(List.of(count));

        Collection<ReviewerCount> expected = List.of(ReviewerCount.of("A", 10L));
        assertEquals(expected, replacementStatsService.countReplacementsGroupedByReviewer(WikipediaLanguage.SPANISH));
    }
}
