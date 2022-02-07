package es.bvalero.replacer.page.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.PageModel;
import es.bvalero.replacer.repository.PageRepository;
import es.bvalero.replacer.repository.ResultCount;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageMostUnreviewedServiceTest {

    @Mock
    private PageRepository pageRepository;

    @InjectMocks
    private PageMostUnreviewedService pageMostUnreviewedService;

    @BeforeEach
    public void setUp() {
        pageMostUnreviewedService = new PageMostUnreviewedService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCountPagesWithMoreReplacementsToReview() {
        PageModel page = PageModel
            .builder()
            .lang(WikipediaLanguage.getDefault().getCode())
            .pageId(1)
            .title("T")
            .lastUpdate(LocalDate.now())
            .build();
        Collection<ResultCount<PageModel>> counts = List.of(ResultCount.of(page, 10));
        when(pageRepository.countPagesWithMoreReplacementsToReview(any(WikipediaLanguage.class), anyInt()))
            .thenReturn(counts);

        Collection<PageCount> expected = List.of(PageCount.of(1, "T", 10));
        assertEquals(
            expected,
            pageMostUnreviewedService.countPagesWithMoreReplacementsToReview(WikipediaLanguage.getDefault())
        );

        verify(pageRepository).countPagesWithMoreReplacementsToReview(any(WikipediaLanguage.class), anyInt());
    }
}
