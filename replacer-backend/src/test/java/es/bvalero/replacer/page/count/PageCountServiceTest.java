package es.bvalero.replacer.page.count;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import es.bvalero.replacer.repository.ResultCount;
import es.bvalero.replacer.user.UserRightsService;
import java.util.Collection;
import java.util.Collections;
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
    private ReplacementTypeRepository replacementTypeRepository;

    @InjectMocks
    private PageCountService pageCountService;

    @BeforeEach
    public void setUp() {
        pageCountService = new PageCountService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCountReplacementsGroupedByType() {
        ReplacementType type = ReplacementType.of(ReplacementKind.DATE, "Y");
        ResultCount<ReplacementType> count = ResultCount.of(type, 100);
        Collection<ResultCount<ReplacementType>> counts = Collections.singletonList(count);

        when(replacementTypeRepository.countReplacementsByType(WikipediaLanguage.getDefault())).thenReturn(counts);

        KindCount kindCount = KindCount.of(ReplacementKind.DATE.getCode());
        kindCount.add(SubtypeCount.of("Y", 100));
        Collection<KindCount> expected = Collections.singletonList(kindCount);

        assertEquals(expected, pageCountService.countReplacementsGroupedByType(WikipediaLanguage.getDefault(), "X"));

        verify(userRightsService).isBot(WikipediaLanguage.getDefault(), "X");
        verify(replacementTypeRepository).countReplacementsByType(WikipediaLanguage.getDefault());
    }

    @Test
    void testCountReplacementsGroupedByTypeForBots() {
        ReplacementType type = ReplacementType.of(ReplacementKind.DATE, "Y");
        ResultCount<ReplacementType> count = ResultCount.of(type, 100);
        ReplacementType typeForBots = mock(ReplacementType.class);
        when(typeForBots.getKind()).thenReturn(ReplacementKind.DATE);
        when(typeForBots.getSubtype()).thenReturn("Z");
        when(typeForBots.isForBots()).thenReturn(true);
        ResultCount<ReplacementType> count2 = ResultCount.of(typeForBots, 200);
        Collection<ResultCount<ReplacementType>> counts = List.of(count, count2);

        String user = "X";
        String bot = "Z";

        when(replacementTypeRepository.countReplacementsByType(WikipediaLanguage.getDefault())).thenReturn(counts);
        when(userRightsService.isBot(WikipediaLanguage.getDefault(), user)).thenReturn(false);
        when(userRightsService.isBot(WikipediaLanguage.getDefault(), bot)).thenReturn(true);

        KindCount kindCount = KindCount.of(ReplacementKind.DATE.getCode());
        kindCount.add(SubtypeCount.of("Y", 100));
        KindCount kindCountBot = KindCount.of(ReplacementKind.DATE.getCode());
        kindCountBot.add(SubtypeCount.of("Y", 100));
        kindCountBot.add(SubtypeCount.of("Z", 200));
        Collection<KindCount> expected = List.of(kindCount);
        Collection<KindCount> expectedBot = List.of(kindCountBot);

        assertEquals(expected, pageCountService.countReplacementsGroupedByType(WikipediaLanguage.getDefault(), user));
        assertEquals(expectedBot, pageCountService.countReplacementsGroupedByType(WikipediaLanguage.getDefault(), bot));

        verify(userRightsService, times(2)).isBot(any(WikipediaLanguage.class), anyString());
        verify(replacementTypeRepository, times(2)).countReplacementsByType(WikipediaLanguage.getDefault());
    }
}