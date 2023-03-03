package es.bvalero.replacer.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.user.UserId;
import es.bvalero.replacer.user.UserRightsService;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageCountServiceTest {

    @Mock
    private UserRightsService userRightsService;

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
    void testCountReplacementsGroupedByTypeForBots() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();

        StandardType type = StandardType.of(ReplacementKind.STYLE, "Y");
        ResultCount<StandardType> count = ResultCount.of(type, 100);
        StandardType typeForBots = StandardType.of(ReplacementKind.STYLE, "Z");
        ResultCount<StandardType> count2 = ResultCount.of(typeForBots, 200);
        Collection<ResultCount<StandardType>> counts = List.of(count, count2);

        UserId user = UserId.of(lang, "user");
        UserId bot = UserId.of(lang, "bot");

        when(pageCountRepository.countPagesNotReviewedByType(lang)).thenReturn(counts);
        when(userRightsService.isTypeForbidden(type, user)).thenReturn(false);
        when(userRightsService.isTypeForbidden(typeForBots, user)).thenReturn(true);
        when(userRightsService.isTypeForbidden(type, bot)).thenReturn(false);
        when(userRightsService.isTypeForbidden(typeForBots, bot)).thenReturn(false);

        assertEquals(List.of(count), pageCountService.countPagesNotReviewedByType(user));
        assertEquals(counts, pageCountService.countPagesNotReviewedByType(bot));

        verify(userRightsService, times(4)).isTypeForbidden(any(ReplacementType.class), any(UserId.class));
        verify(pageCountRepository, times(2)).countPagesNotReviewedByType(lang);
    }
}
