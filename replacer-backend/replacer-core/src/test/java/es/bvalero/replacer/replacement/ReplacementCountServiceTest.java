package es.bvalero.replacer.replacement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReplacementCountServiceTest {

    // Dependency injection
    private ReplacementCountRepository replacementCountRepository;

    private ReplacementCountService replacementCountService;

    @BeforeEach
    public void setUp() {
        replacementCountRepository = mock(ReplacementCountRepository.class);
        replacementCountService = new ReplacementCountService(replacementCountRepository);
    }

    @Test
    void testCountReplacementsReviewed() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        int count = new Random().nextInt();

        when(replacementCountRepository.countReviewed(lang)).thenReturn(count);

        assertEquals(count, replacementCountService.countReplacementsReviewed(lang));

        verify(replacementCountRepository).countReviewed(lang);
    }

    @Test
    void testCountReplacementsToReview() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        int count = new Random().nextInt();

        when(replacementCountRepository.countNotReviewed(lang)).thenReturn(count);

        assertEquals(count, replacementCountService.countReplacementsNotReviewed(lang));

        verify(replacementCountRepository).countNotReviewed(lang);
    }

    @Test
    void testCountReplacementsGroupedByReviewer() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        Collection<ResultCount<String>> counts = List.of(ResultCount.of("A", 10));
        when(replacementCountRepository.countGroupedByReviewer(lang)).thenReturn(counts);

        assertEquals(counts, replacementCountService.countReplacementsGroupedByReviewer(lang));

        verify(replacementCountRepository).countGroupedByReviewer(lang);
    }

    @Test
    void testCountReplacementsGroupedByPage() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        IndexedPage page = IndexedPage
            .builder()
            .pageKey(PageKey.of(lang, 1))
            .title("T")
            .lastUpdate(LocalDate.now())
            .build();
        Collection<ResultCount<IndexedPage>> counts = List.of(ResultCount.of(page, 10));
        when(replacementCountRepository.countNotReviewedGroupedByPage(lang, ReplacementCountService.NUM_RESULTS))
            .thenReturn(counts);

        assertEquals(counts, replacementCountService.countReplacementsNotReviewedGroupedByPage(lang));

        verify(replacementCountRepository).countNotReviewedGroupedByPage(lang, ReplacementCountService.NUM_RESULTS);
    }
}
