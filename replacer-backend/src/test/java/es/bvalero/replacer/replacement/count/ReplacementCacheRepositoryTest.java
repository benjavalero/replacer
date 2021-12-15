package es.bvalero.replacer.replacement.count;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.repository.ReplacementRepository;
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
        ResultCount<ReplacementType> count1 = ResultCount.of(ReplacementType.of(ReplacementKind.DATE, "Y"), 2L);
        ResultCount<ReplacementType> count2 = ResultCount.of(ReplacementType.of(ReplacementKind.DATE, "Z"), 1L);
        Collection<ResultCount<ReplacementType>> counts = List.of(count1, count2);
        when(replacementRepository.countReplacementsByType(lang)).thenReturn(counts);

        replacementCacheRepository.scheduledUpdateReplacementCount();

        LanguageCount langCount = replacementCacheRepository.getLanguageCount(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains(kind));
        assertEquals(2, langCount.get(kind).size());
        assertEquals(2L, langCount.get(kind).get("Y").get().getCount());
        assertEquals(1L, langCount.get(kind).get("Z").get().getCount());

        // Decrease a replacement count
        replacementCacheRepository.decrementSubtypeCount(lang, kind, "Y");

        langCount = replacementCacheRepository.getLanguageCount(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains(kind));
        assertEquals(2, langCount.get(kind).size());
        assertEquals(1L, langCount.get(kind).get("Y").get().getCount());
        assertEquals(1L, langCount.get(kind).get("Z").get().getCount());

        // Decrease a replacement count emptying it
        replacementCacheRepository.decrementSubtypeCount(lang, kind, "Z");

        langCount = replacementCacheRepository.getLanguageCount(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains(kind));
        assertEquals(1, langCount.get(kind).size());
        assertEquals(1L, langCount.get(kind).get("Y").get().getCount());
        assertTrue(langCount.get(kind).get("Z").isEmpty());

        // Remove a replacement count not existing in cache
        replacementCacheRepository.removeCachedReplacementCount(lang, "A", "B");

        langCount = replacementCacheRepository.getLanguageCount(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains(kind));
        assertEquals(1, langCount.get(kind).size());

        // Remove a replacement count existing in cache
        replacementCacheRepository.removeCachedReplacementCount(lang, kind, "Y");

        langCount = replacementCacheRepository.getLanguageCount(lang);
        assertTrue(langCount.isEmpty());
    }
}
