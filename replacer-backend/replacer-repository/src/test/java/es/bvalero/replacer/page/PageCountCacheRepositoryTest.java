package es.bvalero.replacer.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.ReplacementKind;
import es.bvalero.replacer.finder.StandardType;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageCountCacheRepositoryTest {

    // Dependency injection
    private PageCountRepository pageCountRepository;

    private PageCountCacheRepository pageCountCacheRepository;

    @BeforeEach
    public void setUp() {
        pageCountRepository = mock(PageCountRepository.class);
        pageCountCacheRepository = new PageCountCacheRepository(pageCountRepository);
    }

    @Test
    void testGetCachedReplacementCount() {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();

        ReplacementKind kind = ReplacementKind.SIMPLE;
        StandardType type1 = StandardType.of(kind, "Y");
        StandardType type2 = StandardType.of(kind, "Z");
        ResultCount<StandardType> count1 = ResultCount.of(type1, 2);
        ResultCount<StandardType> count2 = ResultCount.of(type2, 1);
        Collection<ResultCount<StandardType>> counts = List.of(count1, count2);
        when(pageCountRepository.countNotReviewedGroupedByType(lang)).thenReturn(counts);

        Collection<ResultCount<StandardType>> typeCounts = pageCountCacheRepository.countNotReviewedGroupedByType(lang);
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
        pageCountCacheRepository.decrementPageCountByType(lang, type1);

        typeCounts = pageCountCacheRepository.countNotReviewedGroupedByType(lang);
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
        pageCountCacheRepository.decrementPageCountByType(lang, type2);

        typeCounts = pageCountCacheRepository.countNotReviewedGroupedByType(lang);
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
        assertEquals(0, pageCountCacheRepository.countNotReviewedByType(lang, type2));

        // Remove a replacement count not existing in cache
        StandardType nonExisting = StandardType.of(ReplacementKind.SIMPLE, "B");
        pageCountCacheRepository.removePageCountByType(lang, nonExisting);

        typeCounts = pageCountCacheRepository.countNotReviewedGroupedByType(lang);
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
        StandardType existing = StandardType.of(ReplacementKind.SIMPLE, "Y");
        pageCountCacheRepository.removePageCountByType(lang, existing);

        typeCounts = pageCountCacheRepository.countNotReviewedGroupedByType(lang);
        assertTrue(typeCounts.isEmpty());
    }
}
