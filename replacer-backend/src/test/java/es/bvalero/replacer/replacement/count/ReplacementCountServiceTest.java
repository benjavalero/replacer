package es.bvalero.replacer.replacement.count;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.PageModel;
import es.bvalero.replacer.repository.ReplacementCountRepository;
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

class ReplacementCountServiceTest {

    @Mock
    private ReplacementCountRepository replacementCountRepository;

    @InjectMocks
    private ReplacementCountService replacementCountService;

    @BeforeEach
    public void setUp() {
        replacementCountService = new ReplacementCountService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCountReplacementsReviewed() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        int count = new Random().nextInt();

        when(replacementCountRepository.countReplacementsReviewed(lang)).thenReturn(count);

        assertEquals(count, replacementCountService.countReplacementsReviewed(lang));

        verify(replacementCountRepository).countReplacementsReviewed(lang);
    }

    @Test
    void testCountReplacementsToReview() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        int count = new Random().nextInt();

        when(replacementCountRepository.countReplacementsNotReviewed(lang)).thenReturn(count);

        assertEquals(count, replacementCountService.countReplacementsNotReviewed(lang));

        verify(replacementCountRepository).countReplacementsNotReviewed(lang);
    }

    @Test
    void testCountReplacementsGroupedByReviewer() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();
        Collection<ResultCount<String>> counts = List.of(ResultCount.of("A", 10));
        when(replacementCountRepository.countReplacementsByReviewer(lang)).thenReturn(counts);

        assertEquals(counts, replacementCountService.countReplacementsGroupedByReviewer(lang));

        verify(replacementCountRepository).countReplacementsByReviewer(lang);
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
        when(replacementCountRepository.countReplacementsByPage(lang, ReplacementCountService.NUM_RESULTS))
            .thenReturn(counts);

        assertEquals(counts, replacementCountService.countReplacementsGroupedByPage(lang));

        verify(replacementCountRepository).countReplacementsByPage(lang, ReplacementCountService.NUM_RESULTS);
    }
}
