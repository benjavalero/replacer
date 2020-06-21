package es.bvalero.replacer.dump;

import es.bvalero.replacer.replacement.ReplacementDao;
import es.bvalero.replacer.replacement.ReplacementEntity;

import java.util.Collections;
import java.util.List;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
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
        // In DB: replacements for article 2 (first load) and 1001 (second load)
        // We ask for the article 1 and 1001, so the article 2 will be cleaned.
        ReplacementEntity replacement = new ReplacementEntity(2, "", "", 0);
        ReplacementEntity replacement2 = new ReplacementEntity(1001, "", "", 0);
        List<ReplacementEntity> dbReplacements = Collections.singletonList(replacement);
        List<ReplacementEntity> dbReplacements2 = Collections.singletonList(replacement2);
        Mockito.when(replacementDao.findByArticles(1, 1000, WikipediaLanguage.SPANISH)).thenReturn(dbReplacements);
        Mockito.when(replacementDao.findByArticles(1001, 2000, WikipediaLanguage.SPANISH)).thenReturn(dbReplacements2);

        List<ReplacementEntity> replacements = replacementCache.findByArticleId(1, WikipediaLanguage.SPANISH);
        Assertions.assertTrue(replacements.isEmpty());

        replacements = replacementCache.findByArticleId(1001, WikipediaLanguage.SPANISH);
        Assertions.assertEquals(dbReplacements2, replacements);

        // Check that the article 2 has been cleaned
        Mockito.verify(replacementDao).reviewArticlesReplacementsAsSystem(Collections.singleton(2), WikipediaLanguage.SPANISH);
    }

    @Test
    void testFindDatabaseReplacementsWithEmptyLoad() {
        // In DB: replacement for article 1001
        // So the first load is enlarged
        ReplacementEntity replacement = new ReplacementEntity(1001, "", "", 0);
        List<ReplacementEntity> dbReplacements = Collections.singletonList(replacement);
        Mockito.when(replacementDao.findByArticles(1, 2000, WikipediaLanguage.SPANISH)).thenReturn(dbReplacements);

        List<ReplacementEntity> replacements = replacementCache.findByArticleId(1001, WikipediaLanguage.SPANISH);
        Assertions.assertEquals(dbReplacements, replacements);
    }
}
