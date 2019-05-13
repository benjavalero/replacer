package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReplacementFinderServiceTest {

    @Mock
    private List<ArticleReplacementFinder> articleReplacementFinders;

    @Mock
    private List<IgnoredReplacementFinder> ignoredReplacementFinders;

    @InjectMocks
    private ReplacementFinderService replacementFinderService;

    @Before
    public void setUp() {
        replacementFinderService = new ReplacementFinderService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindReplacementsEmpty() {
        Mockito.when(articleReplacementFinders.iterator()).thenReturn(Collections.emptyIterator());

        Assert.assertTrue(replacementFinderService.findReplacements("").isEmpty());
    }

    @Test
    public void testFindReplacements() {
        ArticleReplacement replacement = Mockito.mock(ArticleReplacement.class);
        ArticleReplacementFinder finder = Mockito.mock(ArticleReplacementFinder.class);
        Mockito.when(finder.findReplacements(Mockito.anyString()))
                .thenReturn(Collections.singletonList(replacement));
        Mockito.when(articleReplacementFinders.iterator())
                .thenReturn(Collections.singletonList(finder).iterator());
        Mockito.when(ignoredReplacementFinders.iterator()).thenReturn(Collections.emptyIterator());

        List<ArticleReplacement> replacements = replacementFinderService.findReplacements("");

        Assert.assertFalse(replacements.isEmpty());
        Assert.assertEquals(1, replacements.size());
        Assert.assertTrue(replacements.contains(replacement));
    }

    @Test
    public void testFindReplacementsIgnoringExceptions() {
        ArticleReplacement replacement1 = Mockito.mock(ArticleReplacement.class);
        ArticleReplacement replacement2 = Mockito.mock(ArticleReplacement.class);
        ArticleReplacementFinder finder = Mockito.mock(ArticleReplacementFinder.class);
        Mockito.when(finder.findReplacements(Mockito.anyString()))
                .thenReturn(Arrays.asList(replacement1, replacement2));
        Mockito.when(articleReplacementFinders.iterator())
                .thenReturn(Collections.singletonList(finder).iterator());

        MatchResult ignored1 = Mockito.mock(MatchResult.class);
        IgnoredReplacementFinder ignoredFinder = Mockito.mock(IgnoredReplacementFinder.class);
        List<MatchResult> ignored = Collections.singletonList(ignored1);
        Mockito.when(ignoredFinder.findIgnoredReplacements(Mockito.anyString()))
                .thenReturn(ignored);
        Mockito.when(ignoredReplacementFinders.iterator())
                .thenReturn(Collections.singletonList(ignoredFinder).iterator());

        Mockito.when(replacement1.isContainedIn(ignored)).thenReturn(true);
        Mockito.when(replacement2.isContainedIn(ignored)).thenReturn(false);

        List<ArticleReplacement> replacements = replacementFinderService.findReplacements("");

        Assert.assertFalse(replacements.isEmpty());
        Assert.assertEquals(1, replacements.size());
        Assert.assertTrue(replacements.contains(replacement2));
    }

}
