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
    private List<ReplacementFinder> replacementFinders;

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
        Mockito.when(replacementFinders.iterator()).thenReturn(Collections.emptyIterator());

        Assert.assertTrue(replacementFinderService.findReplacements("").isEmpty());
    }

    @Test
    public void testFindReplacements() {
        Replacement replacement = Replacement.builder().build();
        ReplacementFinder finder = Mockito.mock(ReplacementFinder.class);
        Mockito.when(finder.findReplacements(Mockito.anyString()))
                .thenReturn(Collections.singletonList(replacement));
        Mockito.when(replacementFinders.iterator())
                .thenReturn(Collections.singletonList(finder).iterator());
        Mockito.when(ignoredReplacementFinders.iterator()).thenReturn(Collections.emptyIterator());

        List<Replacement> replacements = replacementFinderService.findReplacements("");

        Assert.assertFalse(replacements.isEmpty());
        Assert.assertEquals(1, replacements.size());
        Assert.assertTrue(replacements.contains(replacement));
    }

    @Test
    public void testFindReplacementsIgnoringExceptions() {
        MatchResult ignored1 = MatchResult.of(0, "AB");
        Replacement replacement1 = Replacement.builder().start(1).text("B").build(); // Contained in ignored
        Replacement replacement2 = Replacement.builder().start(2).text("C").build(); // Not contained in ignored
        ReplacementFinder finder = Mockito.mock(ReplacementFinder.class);
        Mockito.when(finder.findReplacements(Mockito.anyString()))
                .thenReturn(Arrays.asList(replacement1, replacement2));
        Mockito.when(replacementFinders.iterator())
                .thenReturn(Collections.singletonList(finder).iterator());

        IgnoredReplacementFinder ignoredFinder = Mockito.mock(IgnoredReplacementFinder.class);
        List<MatchResult> ignored = Collections.singletonList(ignored1);
        Mockito.when(ignoredFinder.findIgnoredReplacements(Mockito.anyString()))
                .thenReturn(ignored);
        Mockito.when(ignoredReplacementFinders.iterator())
                .thenReturn(Collections.singletonList(ignoredFinder).iterator());

        List<Replacement> replacements = replacementFinderService.findReplacements("");

        Assert.assertFalse(replacements.isEmpty());
        Assert.assertEquals(1, replacements.size());
        Assert.assertTrue(replacements.contains(replacement2));
    }

    @Test
    public void testCompareArticleReplacements() {
        Replacement result1 = Replacement.builder().start(0).text("A").build();
        Replacement result2 = Replacement.builder().start(0).text("AB").build();
        Replacement result3 = Replacement.builder().start(1).text("BC").build();
        Replacement result4 = Replacement.builder().start(1).text("BCD").build();
        Replacement result5 = Replacement.builder().start(2).text("C").build();
        List<Replacement> results = Arrays.asList(result1, result2, result3, result4, result5);

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
        Replacement result1 = Replacement.builder().start(0).text("A").build();
        Replacement result2 = Replacement.builder().start(1).text("BC").build();
        List<Replacement> results = Arrays.asList(result1, result2);

        Assert.assertFalse(replacementFinderService.isReplacementContainedInListIgnoringItself(result1, results));
        Assert.assertFalse(replacementFinderService.isReplacementContainedInListIgnoringItself(result2, results));
        Replacement result3 = Replacement.builder().start(1).text("B").build();
        Assert.assertTrue(replacementFinderService.isReplacementContainedInListIgnoringItself(result3, results));
        Replacement result4 = Replacement.builder().start(0).text("AB").build();
        Assert.assertFalse(replacementFinderService.isReplacementContainedInListIgnoringItself(result4, results));
        Replacement result5 = Replacement.builder().start(0).text("ABC").build();
        Assert.assertFalse(replacementFinderService.isReplacementContainedInListIgnoringItself(result5, results));
        Replacement result6 = Replacement.builder().start(2).text("C").build();
        Assert.assertTrue(replacementFinderService.isReplacementContainedInListIgnoringItself(result6, results));
    }

}
