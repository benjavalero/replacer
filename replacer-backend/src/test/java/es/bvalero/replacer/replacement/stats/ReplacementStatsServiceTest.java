package es.bvalero.replacer.replacement.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.ReplacementStatsRepository;
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
    private ReplacementStatsRepository replacementStatsRepository;

    @InjectMocks
    private ReplacementStatsService replacementStatsService;

    @BeforeEach
    public void setUp() {
        replacementStatsService = new ReplacementStatsService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCountReplacementsReviewed() {
        int count = new Random().nextInt();

        when(replacementStatsRepository.countReplacementsReviewed(any(WikipediaLanguage.class))).thenReturn(count);

        assertEquals(count, replacementStatsService.countReplacementsReviewed(WikipediaLanguage.SPANISH));

        verify(replacementStatsRepository).countReplacementsReviewed(any(WikipediaLanguage.class));
    }

    @Test
    void testCountReplacementsToReview() {
        int count = new Random().nextInt();

        when(replacementStatsRepository.countReplacementsNotReviewed(any(WikipediaLanguage.class))).thenReturn(count);

        assertEquals(count, replacementStatsService.countReplacementsNotReviewed(WikipediaLanguage.SPANISH));

        verify(replacementStatsRepository).countReplacementsNotReviewed(any(WikipediaLanguage.class));
    }

    @Test
    void testCountReplacementsGroupedByReviewer() {
        Collection<ResultCount<String>> counts = List.of(ResultCount.of("A", 10));
        when(replacementStatsRepository.countReplacementsByReviewer(any(WikipediaLanguage.class))).thenReturn(counts);

        assertEquals(counts, replacementStatsService.countReplacementsGroupedByReviewer(WikipediaLanguage.SPANISH));

        verify(replacementStatsRepository).countReplacementsByReviewer(any(WikipediaLanguage.class));
    }
}
