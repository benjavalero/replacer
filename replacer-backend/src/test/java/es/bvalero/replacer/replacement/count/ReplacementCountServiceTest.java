package es.bvalero.replacer.replacement.count;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import es.bvalero.replacer.repository.ResultCount;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReplacementCountServiceTest {

    @Mock
    private ReplacementTypeRepository replacementTypeRepository;

    @InjectMocks
    private ReplacementCountService replacementCountService;

    @BeforeEach
    public void setUp() {
        replacementCountService = new ReplacementCountService();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCountReplacementsGroupedByType() {
        ReplacementType type = ReplacementType.of(ReplacementKind.DATE, "Y");
        ResultCount<ReplacementType> count = ResultCount.of(type, 100);
        Collection<ResultCount<ReplacementType>> counts = Collections.singletonList(count);

        when(replacementTypeRepository.countReplacementsByType(WikipediaLanguage.getDefault())).thenReturn(counts);

        KindCount kindCount = KindCount.of(ReplacementKind.DATE.getLabel());
        kindCount.add(TypeCount.of("Y", 100));
        Collection<KindCount> expected = Collections.singletonList(kindCount);

        assertEquals(expected, replacementCountService.countReplacementsGroupedByType(WikipediaLanguage.getDefault()));

        verify(replacementTypeRepository).countReplacementsByType(WikipediaLanguage.getDefault());
    }
}
