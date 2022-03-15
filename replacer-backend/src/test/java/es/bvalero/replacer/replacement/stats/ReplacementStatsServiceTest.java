package es.bvalero.replacer.replacement.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.PageModel;
import es.bvalero.replacer.repository.ReplacementStatsRepository;
import es.bvalero.replacer.repository.ResultCount;
import java.time.LocalDate;
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
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        int count = new Random().nextInt();

        when(replacementStatsRepository.countReplacementsReviewed(lang)).thenReturn(count);

        assertEquals(count, replacementStatsService.countReplacementsReviewed(lang));

        verify(replacementStatsRepository).countReplacementsReviewed(lang);
    }

    @Test
    void testCountReplacementsToReview() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        int count = new Random().nextInt();

        when(replacementStatsRepository.countReplacementsNotReviewed(lang)).thenReturn(count);

        assertEquals(count, replacementStatsService.countReplacementsNotReviewed(lang));

        verify(replacementStatsRepository).countReplacementsNotReviewed(lang);
    }

    @Test
    void testCountReplacementsGroupedByReviewer() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        Collection<ResultCount<String>> counts = List.of(ResultCount.of("A", 10));
        when(replacementStatsRepository.countReplacementsByReviewer(lang)).thenReturn(counts);

        assertEquals(counts, replacementStatsService.countReplacementsGroupedByReviewer(lang));

        verify(replacementStatsRepository).countReplacementsByReviewer(lang);
    }

    @Test
    void testCountReplacementsGroupedByPage() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        PageModel page = PageModel
            .builder()
            .lang(lang.getCode())
            .pageId(1)
            .title("T")
            .lastUpdate(LocalDate.now())
            .build();
        Collection<ResultCount<PageModel>> counts = List.of(ResultCount.of(page, 10));
        when(replacementStatsRepository.countReplacementsByPage(lang, ReplacementStatsService.NUM_RESULTS))
            .thenReturn(counts);

        assertEquals(counts, replacementStatsService.countReplacementsGroupedByPage(lang));

        verify(replacementStatsRepository).countReplacementsByPage(lang, ReplacementStatsService.NUM_RESULTS);
    }
}
