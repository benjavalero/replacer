package es.bvalero.replacer.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageCountCacheRepositoryTest {

    @Mock
    private PageCountRepository pageCountRepository;

    @InjectMocks
    private PageCountCacheRepository pageCountCacheRepository;

    @BeforeEach
    public void setUp() {
        pageCountCacheRepository = new PageCountCacheRepository();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCachedReplacementCount() throws ReplacerException {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();

        ReplacementKind kind = ReplacementKind.STYLE;
        ReplacementType type1 = ReplacementType.of(kind, "Y");
        ReplacementType type2 = ReplacementType.of(kind, "Z");
        ResultCount<ReplacementType> count1 = ResultCount.of(type1, 2);
        ResultCount<ReplacementType> count2 = ResultCount.of(type2, 1);
        Collection<ResultCount<ReplacementType>> counts = List.of(count1, count2);
        when(pageCountRepository.countPagesNotReviewedByType(lang)).thenReturn(counts);

        pageCountCacheRepository.scheduledUpdateReplacementCount();

        Collection<ResultCount<ReplacementType>> typeCounts = pageCountCacheRepository.countPagesNotReviewedByType(
            lang
        );
        assertEquals(2, typeCounts.size());
        assertEquals(1, typeCounts.stream().map(rc -> rc.getKey().getKind()).distinct().count());
        assertEquals(kind, typeCounts.stream().map(rc -> rc.getKey().getKind()).distinct().findAny().orElse(null));
        assertEquals(
            2,
            typeCounts
                .stream()
                .filter(rc -> rc.getKey().equals(type1))
                .map(ResultCount::getCount)
                .findAny()
                .orElse(null)
        );
        assertEquals(
            1,
            typeCounts
                .stream()
                .filter(rc -> rc.getKey().equals(type2))
                .map(ResultCount::getCount)
                .findAny()
                .orElse(null)
        );

        // Decrease a replacement count
        pageCountCacheRepository.decrementPageCount(lang, type1);

        typeCounts = pageCountCacheRepository.countPagesNotReviewedByType(lang);
        assertEquals(2, typeCounts.size());
        assertEquals(1, typeCounts.stream().map(rc -> rc.getKey().getKind()).distinct().count());
        assertEquals(kind, typeCounts.stream().map(rc -> rc.getKey().getKind()).distinct().findAny().orElse(null));
        assertEquals(
            1,
            typeCounts
                .stream()
                .filter(rc -> rc.getKey().equals(type1))
                .map(ResultCount::getCount)
                .findAny()
                .orElse(null)
        );
        assertEquals(
            1,
            typeCounts
                .stream()
                .filter(rc -> rc.getKey().equals(type2))
                .map(ResultCount::getCount)
                .findAny()
                .orElse(null)
        );

        // Decrease a replacement count emptying it
        pageCountCacheRepository.decrementPageCount(lang, type2);

        typeCounts = pageCountCacheRepository.countPagesNotReviewedByType(lang);
        assertEquals(1, typeCounts.size());
        assertEquals(kind, typeCounts.stream().map(rc -> rc.getKey().getKind()).distinct().findAny().orElse(null));
        assertEquals(
            1,
            typeCounts
                .stream()
                .filter(rc -> rc.getKey().equals(type1))
                .map(ResultCount::getCount)
                .findAny()
                .orElse(null)
        );
        assertTrue(typeCounts.stream().noneMatch(rc -> rc.getKey().equals(type2)));

        // Remove a replacement count not existing in cache
        ReplacementType nonExisting = ReplacementType.of(ReplacementKind.SIMPLE, "B");
        pageCountCacheRepository.removePageCount(lang, nonExisting);

        typeCounts = pageCountCacheRepository.countPagesNotReviewedByType(lang);
        assertEquals(1, typeCounts.size());
        assertEquals(kind, typeCounts.stream().map(rc -> rc.getKey().getKind()).distinct().findAny().orElse(null));
        assertEquals(
            1,
            typeCounts
                .stream()
                .filter(rc -> rc.getKey().equals(type1))
                .map(ResultCount::getCount)
                .findAny()
                .orElse(null)
        );

        // Remove a replacement count existing in cache
        ReplacementType existing = ReplacementType.of(ReplacementKind.STYLE, "Y");
        pageCountCacheRepository.removePageCount(lang, existing);

        typeCounts = pageCountCacheRepository.countPagesNotReviewedByType(lang);
        assertTrue(typeCounts.isEmpty());
    }
}
