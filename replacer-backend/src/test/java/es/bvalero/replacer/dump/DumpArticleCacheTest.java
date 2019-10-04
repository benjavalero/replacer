package es.bvalero.replacer.dump;

import es.bvalero.replacer.article.ArticleIndexService;
import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.article.ReplacementEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class DumpArticleCacheTest {

    @Mock
    private ArticleService articleService;

    @Mock
    private ArticleIndexService articleIndexService;

    @InjectMocks
    private DumpArticleCache dumpArticleCache;

    @Before
    public void setUp() {
        dumpArticleCache = new DumpArticleCache();
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
        Mockito.when(articleService.findDatabaseReplacementByArticles(1, 1000))
                .thenReturn(dbReplacements);
        Mockito.when(articleService.findDatabaseReplacementByArticles(1001, 2000))
                .thenReturn(dbReplacements2);

        Collection<ReplacementEntity> replacements = dumpArticleCache.findDatabaseReplacements(1);
        Assert.assertTrue(replacements.isEmpty());

        replacements = dumpArticleCache.findDatabaseReplacements(1001);
        Assert.assertEquals(new HashSet<>(dbReplacements2), replacements);

        // Check that the article 2 has been cleaned
        Mockito.verify(articleIndexService).reviewArticlesAsSystem(Collections.singleton(2));
    }

    @Test
    public void testFindDatabaseReplacementsWithEmptyLoad() {
        // In DB: replacement for article 1001
        // So the first load is enlarged
        ReplacementEntity replacement = new ReplacementEntity(1001, "", "", 0);
        List<ReplacementEntity> dbReplacements = Collections.singletonList(replacement);
        Mockito.when(articleService.findDatabaseReplacementByArticles(1, 2000))
                .thenReturn(dbReplacements);

        Collection<ReplacementEntity> replacements = dumpArticleCache.findDatabaseReplacements(1001);
        Assert.assertEquals(new HashSet<>(dbReplacements), replacements);
    }

}
