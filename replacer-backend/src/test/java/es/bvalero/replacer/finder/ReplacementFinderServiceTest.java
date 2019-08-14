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
        ArticleReplacement replacement = ArticleReplacement.builder().build();
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
        MatchResult ignored1 = new MatchResult(0, "AB");
        ArticleReplacement replacement1 = ArticleReplacement.builder().start(1).text("B").build(); // Contained in ignored
        ArticleReplacement replacement2 = ArticleReplacement.builder().start(2).text("C").build(); // Not contained in ignored
        ArticleReplacementFinder finder = Mockito.mock(ArticleReplacementFinder.class);
        Mockito.when(finder.findReplacements(Mockito.anyString()))
                .thenReturn(Arrays.asList(replacement1, replacement2));
        Mockito.when(articleReplacementFinders.iterator())
                .thenReturn(Collections.singletonList(finder).iterator());

        IgnoredReplacementFinder ignoredFinder = Mockito.mock(IgnoredReplacementFinder.class);
        List<MatchResult> ignored = Collections.singletonList(ignored1);
        Mockito.when(ignoredFinder.findIgnoredReplacements(Mockito.anyString()))
                .thenReturn(ignored);
        Mockito.when(ignoredReplacementFinders.iterator())
                .thenReturn(Collections.singletonList(ignoredFinder).iterator());

        List<ArticleReplacement> replacements = replacementFinderService.findReplacements("");

        Assert.assertFalse(replacements.isEmpty());
        Assert.assertEquals(1, replacements.size());
        Assert.assertTrue(replacements.contains(replacement2));
    }

    @Test
    public void testCompareArticleReplacements() {
        ArticleReplacement result1 = ArticleReplacement.builder().start(0).text("A").build();
        ArticleReplacement result2 = ArticleReplacement.builder().start(0).text("AB").build();
        ArticleReplacement result3 = ArticleReplacement.builder().start(1).text("BC").build();
        ArticleReplacement result4 = ArticleReplacement.builder().start(1).text("BCD").build();
        ArticleReplacement result5 = ArticleReplacement.builder().start(2).text("C").build();
        List<ArticleReplacement> results = Arrays.asList(result1, result2, result3, result4, result5);

        Collections.sort(results);

        // Order descendant by start. If equals, the lower end.
        Assert.assertEquals(result5, results.get(0));
        Assert.assertEquals(result3, results.get(1));
        Assert.assertEquals(result4, results.get(2));
        Assert.assertEquals(result1, results.get(3));
        Assert.assertEquals(result2, results.get(4));
    }

    @Test
    public void testIsContained() {
        ArticleReplacement result1 = ArticleReplacement.builder().start(0).text("A").build();
        ArticleReplacement result2 = ArticleReplacement.builder().start(1).text("BC").build();
        List<ArticleReplacement> results = Arrays.asList(result1, result2);

        Assert.assertFalse(replacementFinderService.isReplacementContainedInListIgnoringItself(result1, results));
        Assert.assertFalse(replacementFinderService.isReplacementContainedInListIgnoringItself(result2, results));
        ArticleReplacement result3 = ArticleReplacement.builder().start(1).text("B").build();
        Assert.assertTrue(replacementFinderService.isReplacementContainedInListIgnoringItself(result3, results));
        ArticleReplacement result4 = ArticleReplacement.builder().start(0).text("AB").build();
        Assert.assertFalse(replacementFinderService.isReplacementContainedInListIgnoringItself(result4, results));
        ArticleReplacement result5 = ArticleReplacement.builder().start(0).text("ABC").build();
        Assert.assertFalse(replacementFinderService.isReplacementContainedInListIgnoringItself(result5, results));
        ArticleReplacement result6 = ArticleReplacement.builder().start(2).text("C").build();
        Assert.assertTrue(replacementFinderService.isReplacementContainedInListIgnoringItself(result6, results));
    }

}
