package es.bvalero.replacer.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
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

        ReplacementType type = ReplacementType.of(ReplacementKind.STYLE, "Y");
        ResultCount<ReplacementType> count = ResultCount.of(type, 100);
        ReplacementType typeForBots = ReplacementType.of(ReplacementKind.STYLE, "Z");
        ResultCount<ReplacementType> count2 = ResultCount.of(typeForBots, 200);
        Collection<ResultCount<ReplacementType>> counts = List.of(count, count2);

        String user = "user";
        String bot = "bot";

        when(pageCountRepository.countPagesNotReviewedByType(lang)).thenReturn(counts);
        when(userRightsService.isTypeForbidden(type, lang, user)).thenReturn(false);
        when(userRightsService.isTypeForbidden(typeForBots, lang, user)).thenReturn(true);
        when(userRightsService.isTypeForbidden(type, lang, bot)).thenReturn(false);
        when(userRightsService.isTypeForbidden(typeForBots, lang, bot)).thenReturn(false);

        assertEquals(List.of(count), pageCountService.countPagesNotReviewedByType(lang, user));
        assertEquals(counts, pageCountService.countPagesNotReviewedByType(lang, bot));

        verify(userRightsService, times(4))
            .isTypeForbidden(any(ReplacementType.class), any(WikipediaLanguage.class), anyString());
        verify(pageCountRepository, times(2)).countPagesNotReviewedByType(lang);
    }
}
