package es.bvalero.replacer.replacement.count.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import es.bvalero.replacer.repository.ResultCount;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReplacementTypeCacheRepositoryTest {

    @Mock
    private ReplacementTypeRepository replacementTypeRepository;

    @InjectMocks
    private ReplacementTypeCacheRepository replacementCacheRepository;

    @BeforeEach
    public void setUp() {
        replacementCacheRepository = new ReplacementTypeCacheRepository();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCachedReplacementCount() throws ReplacerException {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();

        ReplacementKind kind = ReplacementKind.DATE;
        ReplacementType type1 = ReplacementType.of(kind, "Y");
        ReplacementType type2 = ReplacementType.of(kind, "Z");
        ResultCount<ReplacementType> count1 = ResultCount.of(type1, 2);
        ResultCount<ReplacementType> count2 = ResultCount.of(type2, 1);
        Collection<ResultCount<ReplacementType>> counts = List.of(count1, count2);
        when(replacementTypeRepository.countReplacementsByType(lang)).thenReturn(counts);

        replacementCacheRepository.scheduledUpdateReplacementCount();

        KindCount kindCount = replacementCacheRepository.getKindCount(lang);
        assertEquals(1, kindCount.size());
        assertTrue(kindCount.contains(kind));
        assertEquals(2, kindCount.get(kind).size());
        assertEquals(2, kindCount.get(kind).get("Y").orElse(null));
        assertEquals(1, kindCount.get(kind).get("Z").orElse(null));

        // Decrease a replacement count
        replacementCacheRepository.decrementSubtypeCount(lang, type1);

        kindCount = replacementCacheRepository.getKindCount(lang);
        assertEquals(1, kindCount.size());
        assertTrue(kindCount.contains(kind));
        assertEquals(2, kindCount.get(kind).size());
        assertEquals(1, kindCount.get(kind).get("Y").orElse(null));
        assertEquals(1, kindCount.get(kind).get("Z").orElse(null));

        // Decrease a replacement count emptying it
        replacementCacheRepository.decrementSubtypeCount(lang, type2);

        kindCount = replacementCacheRepository.getKindCount(lang);
        assertEquals(1, kindCount.size());
        assertTrue(kindCount.contains(kind));
        assertEquals(1, kindCount.get(kind).size());
        assertEquals(1, kindCount.get(kind).get("Y").orElse(null));
        assertTrue(kindCount.get(kind).get("Z").isEmpty());

        // Remove a replacement count not existing in cache
        ReplacementType nonExisting = ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "B");
        replacementCacheRepository.removeCachedReplacementCount(lang, nonExisting);

        kindCount = replacementCacheRepository.getKindCount(lang);
        assertEquals(1, kindCount.size());
        assertTrue(kindCount.contains(kind));
        assertEquals(1, kindCount.get(kind).size());

        // Remove a replacement count existing in cache
        ReplacementType existing = ReplacementType.of(ReplacementKind.DATE, "Y");
        replacementCacheRepository.removeCachedReplacementCount(lang, existing);

        kindCount = replacementCacheRepository.getKindCount(lang);
        assertTrue(kindCount.isEmpty());
    }
}
