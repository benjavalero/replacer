package es.bvalero.replacer.replacement.count;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.repository.ReplacementRepository;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import es.bvalero.replacer.repository.ResultCount;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReplacementCacheRepositoryTest {

    @Mock
    private ReplacementRepository replacementRepository;

    @Mock
    private ReplacementTypeRepository replacementTypeRepository;

    @InjectMocks
    private ReplacementCacheRepository replacementCacheRepository;

    @BeforeEach
    public void setUp() {
        replacementCacheRepository = new ReplacementCacheRepository();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCachedReplacementCount() throws ReplacerException {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();

        String kind = ReplacementKind.DATE.getLabel();
        ReplacementType type1 = ReplacementType.of(ReplacementKind.DATE, "Y");
        ReplacementType type2 = ReplacementType.of(ReplacementKind.DATE, "Z");
        ResultCount<ReplacementType> count1 = ResultCount.of(type1, 2L);
        ResultCount<ReplacementType> count2 = ResultCount.of(type2, 1L);
        Collection<ResultCount<ReplacementType>> counts = List.of(count1, count2);
        when(replacementTypeRepository.countReplacementsByType(lang)).thenReturn(counts);

        replacementCacheRepository.scheduledUpdateReplacementCount();

        LanguageCount langCount = replacementCacheRepository.getLanguageCount(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains(kind));
        assertEquals(2, langCount.get(kind).size());
        assertEquals(2L, langCount.get(kind).get("Y").get().getCount());
        assertEquals(1L, langCount.get(kind).get("Z").get().getCount());

        // Decrease a replacement count
        replacementCacheRepository.decrementSubtypeCount(lang, type1);

        langCount = replacementCacheRepository.getLanguageCount(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains(kind));
        assertEquals(2, langCount.get(kind).size());
        assertEquals(1L, langCount.get(kind).get("Y").get().getCount());
        assertEquals(1L, langCount.get(kind).get("Z").get().getCount());

        // Decrease a replacement count emptying it
        replacementCacheRepository.decrementSubtypeCount(lang, type2);

        langCount = replacementCacheRepository.getLanguageCount(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains(kind));
        assertEquals(1, langCount.get(kind).size());
        assertEquals(1L, langCount.get(kind).get("Y").get().getCount());
        assertTrue(langCount.get(kind).get("Z").isEmpty());

        // Remove a replacement count not existing in cache
        ReplacementType nonExisting = ReplacementType.of(ReplacementKind.MISSPELLING_SIMPLE, "B");
        replacementCacheRepository.removeCachedReplacementCount(lang, nonExisting);

        langCount = replacementCacheRepository.getLanguageCount(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains(kind));
        assertEquals(1, langCount.get(kind).size());

        // Remove a replacement count existing in cache
        ReplacementType existing = ReplacementType.of(ReplacementKind.DATE, "Y");
        replacementCacheRepository.removeCachedReplacementCount(lang, existing);

        langCount = replacementCacheRepository.getLanguageCount(lang);
        assertTrue(langCount.isEmpty());
    }
}
