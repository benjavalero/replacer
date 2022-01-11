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
        ResultCount<ReplacementType> count1 = ResultCount.of(type1, 2L);
        ResultCount<ReplacementType> count2 = ResultCount.of(type2, 1L);
        Collection<ResultCount<ReplacementType>> counts = List.of(count1, count2);
        when(replacementTypeRepository.countReplacementsByType(lang)).thenReturn(counts);

        replacementCacheRepository.scheduledUpdateReplacementCount();

        LanguageCount langCount = replacementCacheRepository.getKindCount(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains(kind));
        assertEquals(2, langCount.get(kind).size());
        assertEquals(2L, langCount.get(kind).get("Y").orElse(null));
        assertEquals(1L, langCount.get(kind).get("Z").orElse(null));

        // Decrease a replacement count
        replacementCacheRepository.decrementSubtypeCount(lang, type1);

        langCount = replacementCacheRepository.getKindCount(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains(kind));
        assertEquals(2, langCount.get(kind).size());
        assertEquals(1L, langCount.get(kind).get("Y").orElse(null));
        assertEquals(1L, langCount.get(kind).get("Z").orElse(null));

        // Decrease a replacement count emptying it
        replacementCacheRepository.decrementSubtypeCount(lang, type2);

        langCount = replacementCacheRepository.getKindCount(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains(kind));
        assertEquals(1, langCount.get(kind).size());
        assertEquals(1L, langCount.get(kind).get("Y").orElse(null));
        assertTrue(langCount.get(kind).get("Z").isEmpty());

        // Remove a replacement count not existing in cache
        ReplacementType nonExisting = ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "B");
        replacementCacheRepository.removeCachedReplacementCount(lang, nonExisting);

        langCount = replacementCacheRepository.getKindCount(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains(kind));
        assertEquals(1, langCount.get(kind).size());

        // Remove a replacement count existing in cache
        ReplacementType existing = ReplacementType.of(ReplacementKind.DATE, "Y");
        replacementCacheRepository.removeCachedReplacementCount(lang, existing);

        langCount = replacementCacheRepository.getKindCount(lang);
        assertTrue(langCount.isEmpty());
    }
}
