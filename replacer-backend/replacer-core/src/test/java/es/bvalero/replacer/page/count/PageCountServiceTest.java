package es.bvalero.replacer.page.count;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.user.User;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageCountServiceTest {

    @Mock
    private PageCountRepository pageCountRepository;

    @InjectMocks
    private PageCountService pageCountService;

    @BeforeEach
    public void setUp() {
        pageCountService = new PageCountService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCountNotReviewedGroupedByTypeForBots() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();

        StandardType type = StandardType.DATE;
        ResultCount<StandardType> count = ResultCount.of(type, 100);
        StandardType typeForBots = StandardType.ofForBots(ReplacementKind.SIMPLE, "x");
        ResultCount<StandardType> count2 = ResultCount.of(typeForBots, 200);
        Collection<ResultCount<StandardType>> counts = List.of(count, count2);

        User user = User.buildTestUser();
        User bot = User.buildTestBotUser();

        when(pageCountRepository.countNotReviewedGroupedByType(lang)).thenReturn(counts);

        assertEquals(List.of(count), pageCountService.countNotReviewedGroupedByType(user));
        assertEquals(counts, pageCountService.countNotReviewedGroupedByType(bot));

        verify(pageCountRepository, times(2)).countNotReviewedGroupedByType(lang);
    }
}
