package es.bvalero.replacer.replacement.count;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReplacementCountCacheTest {

    @Mock
    private ReplacementCountRepository replacementCountRepository;

    @InjectMocks
    private ReplacementCountCacheRepository replacementCountCacheRepository;

    @BeforeEach
    public void setUp() {
        replacementCountCacheRepository = new ReplacementCountCacheRepository();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetCachedReplacementCount() throws ReplacerException {
        WikipediaLanguage lang = WikipediaLanguage.getDefault();

        TypeSubtypeCount count1 = TypeSubtypeCount.of("X", "Y", 2L);
        TypeSubtypeCount count2 = TypeSubtypeCount.of("X", "Z", 1L);
        List<TypeSubtypeCount> counts = Arrays.asList(count1, count2);
        when(replacementCountRepository.countReplacementsGroupedByType(lang)).thenReturn(LanguageCount.build(counts));

        replacementCountCacheRepository.scheduledUpdateReplacementCount();

        LanguageCount langCount = replacementCountCacheRepository.countReplacementsGroupedByType(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains("X"));
        assertEquals(2, langCount.get("X").size());
        assertEquals(2L, langCount.get("X").get("Y").get().getCount());
        assertEquals(1L, langCount.get("X").get("Z").get().getCount());

        // Decrease a replacement count
        replacementCountCacheRepository.decrementSubtypeCount(lang, "X", "Y");

        langCount = replacementCountCacheRepository.countReplacementsGroupedByType(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains("X"));
        assertEquals(2, langCount.get("X").size());
        assertEquals(1L, langCount.get("X").get("Y").get().getCount());
        assertEquals(1L, langCount.get("X").get("Z").get().getCount());

        // Decrease a replacement count emptying it
        replacementCountCacheRepository.decrementSubtypeCount(lang, "X", "Z");

        langCount = replacementCountCacheRepository.countReplacementsGroupedByType(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains("X"));
        assertEquals(1, langCount.get("X").size());
        assertEquals(1L, langCount.get("X").get("Y").get().getCount());
        assertTrue(langCount.get("X").get("Z").isEmpty());

        // Remove a replacement count not existing in cache
        replacementCountCacheRepository.removeCachedReplacementCount(lang, "A", "B");

        langCount = replacementCountCacheRepository.countReplacementsGroupedByType(lang);
        assertEquals(1, langCount.size());
        assertTrue(langCount.contains("X"));
        assertEquals(1, langCount.get("X").size());

        // Remove a replacement count existing in cache
        replacementCountCacheRepository.removeCachedReplacementCount(lang, "X", "Y");

        langCount = replacementCountCacheRepository.countReplacementsGroupedByType(lang);
        assertTrue(langCount.isEmpty());
    }
}
