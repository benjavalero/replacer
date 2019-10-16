package es.bvalero.replacer.dump;

import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.replacement.ReplacementRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

public class ReplacementCacheTest {

    @Mock
    private ReplacementRepository replacementRepository;

    @Mock
    private ReplacementIndexService replacementIndexService;

    @InjectMocks
    private ReplacementCache replacementCache;

    @Before
    public void setUp() {
        replacementCache = new ReplacementCache();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindDatabaseReplacements() {
        // In DB: replacements for article 2 (first load) and 1001 (second load)
        // We ask for the article 1 and 1001, so the article 2 will be cleaned.
        ReplacementEntity replacement = new ReplacementEntity(2, "", "", 0);
        ReplacementEntity replacement2 = new ReplacementEntity(1001, "", "", 0);
        List<ReplacementEntity> dbReplacements = Collections.singletonList(replacement);
        List<ReplacementEntity> dbReplacements2 = Collections.singletonList(replacement2);
        Mockito.when(replacementRepository.findByArticles(1, 1000))
                .thenReturn(dbReplacements);
        Mockito.when(replacementRepository.findByArticles(1001, 2000))
                .thenReturn(dbReplacements2);

        List<ReplacementEntity> replacements = replacementCache.findByArticleId(1);
        Assert.assertTrue(replacements.isEmpty());

        replacements = replacementCache.findByArticleId(1001);
        Assert.assertEquals(dbReplacements2, replacements);

        // Check that the article 2 has been cleaned
        Mockito.verify(replacementIndexService).reviewArticlesReplacementsAsSystem(Collections.singleton(2));
    }

    @Test
    public void testFindDatabaseReplacementsWithEmptyLoad() {
        // In DB: replacement for article 1001
        // So the first load is enlarged
        ReplacementEntity replacement = new ReplacementEntity(1001, "", "", 0);
        List<ReplacementEntity> dbReplacements = Collections.singletonList(replacement);
        Mockito.when(replacementRepository.findByArticles(1, 2000))
                .thenReturn(dbReplacements);

        List<ReplacementEntity> replacements = replacementCache.findByArticleId(1001);
        Assert.assertEquals(dbReplacements, replacements);
    }

}
