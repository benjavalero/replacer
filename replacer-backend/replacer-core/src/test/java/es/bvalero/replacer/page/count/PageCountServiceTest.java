package es.bvalero.replacer.page.count;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.User;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageCountServiceTest {

    // Dependency injection
    private PageCountRepository pageCountRepository;

    private PageCountService pageCountService;

    @BeforeEach
    public void setUp() {
        pageCountRepository = mock(PageCountRepository.class);
        pageCountService = new PageCountService(pageCountRepository);
    }

    @Test
    void testCountNotReviewedGroupedByTypeForAdmin() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();

        StandardType type = StandardType.DATE;
        ResultCount<StandardType> count = ResultCount.of(type, 100);
        StandardType typeForAdmin = StandardType.ofForAdmin(ReplacementKind.SIMPLE, "x");
        ResultCount<StandardType> count2 = ResultCount.of(typeForAdmin, 200);
        Collection<ResultCount<StandardType>> counts = List.of(count, count2);

        User user = User.buildTestUser();
        User admin = User.buildTestAdminUser();

        when(pageCountRepository.countNotReviewedGroupedByType(lang)).thenReturn(counts);

        assertEquals(List.of(count), pageCountService.countNotReviewedGroupedByType(user));
        assertEquals(counts, pageCountService.countNotReviewedGroupedByType(admin));

        verify(pageCountRepository, times(2)).countNotReviewedGroupedByType(lang);
    }
}
