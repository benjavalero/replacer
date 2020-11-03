package es.bvalero.replacer.dump;

import es.bvalero.replacer.replacement.ReplacementDao;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class ReplacementCacheTest {
    @Mock
    private ReplacementDao replacementDao;

    @InjectMocks
    private ReplacementCache replacementCache;

    @BeforeEach
    public void setUp() {
        replacementCache = new ReplacementCache();
        replacementCache.setChunkSize(1000);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindDatabaseReplacements() {
        // In DB: replacements for page 2 (first load) and 1001 (second load)
        // We ask for the page 1 and 1001, so the page 2 will be cleaned.
        ReplacementEntity replacement = new ReplacementEntity(2, "", "", 0);
        ReplacementEntity replacement2 = new ReplacementEntity(1001, "", "", 0);
        List<ReplacementEntity> dbReplacements = Collections.singletonList(replacement);
        List<ReplacementEntity> dbReplacements2 = Collections.singletonList(replacement2);
        Mockito.when(replacementDao.findByPageInterval(1, 1000, WikipediaLanguage.SPANISH)).thenReturn(dbReplacements);
        Mockito.when(replacementDao.findByPageInterval(1001, 2000, WikipediaLanguage.SPANISH)).thenReturn(dbReplacements2);

        List<ReplacementEntity> replacements = replacementCache.findByPageId(1, WikipediaLanguage.SPANISH);
        Assertions.assertTrue(replacements.isEmpty());

        replacements = replacementCache.findByPageId(1001, WikipediaLanguage.SPANISH);
        Assertions.assertEquals(dbReplacements2, replacements);

        // Check that the page 2 has been cleaned
        Mockito
            .verify(replacementDao)
            .deleteObsoleteByPageId(WikipediaLanguage.SPANISH, Collections.singleton(2));
    }

    @Test
    void testFindDatabaseReplacementsWithEmptyLoad() {
        // In DB: replacement for page 1001
        // So the first load is enlarged
        ReplacementEntity replacement = new ReplacementEntity(1001, "", "", 0);
        List<ReplacementEntity> dbReplacements = Collections.singletonList(replacement);
        Mockito.when(replacementDao.findByPageInterval(1, 2000, WikipediaLanguage.SPANISH)).thenReturn(dbReplacements);

        List<ReplacementEntity> replacements = replacementCache.findByPageId(1001, WikipediaLanguage.SPANISH);
        Assertions.assertEquals(dbReplacements, replacements);
    }
}
