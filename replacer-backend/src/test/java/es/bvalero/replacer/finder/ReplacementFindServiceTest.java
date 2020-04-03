package es.bvalero.replacer.finder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import es.bvalero.replacer.finder.immutables.QuotesFinder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ReplacementFindServiceTest {
    @Mock
    private List<ReplacementFinder> replacementFinders;

    @Mock
    private ImmutableFindService immutableFindService;

    @InjectMocks
    private ReplacementFindService replacementFindService;

    @Before
    public void setUp() {
        replacementFindService = new ReplacementFindService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindReplacementsEmpty() {
        Assert.assertTrue(replacementFindService.findReplacements("").isEmpty());
    }

    @Test
    public void testFindReplacements() {
        Replacement replacement = Replacement.builder().start(0).text("").build();
        ReplacementFinder finder = Mockito.mock(ReplacementFinder.class);
        Mockito.when(finder.findStream(Mockito.anyString())).thenReturn(Stream.of(replacement));
        Mockito.when(replacementFinders.stream()).thenReturn(Stream.of(finder));

        List<Replacement> replacements = replacementFindService.findReplacements(" ");

        Assert.assertFalse(replacements.isEmpty());
        Assert.assertEquals(1, replacements.size());
        Assert.assertTrue(replacements.contains(replacement));
    }

    @Test
    public void testFindReplacementsIgnoringImmutables() {
        Immutable immutable1 = Immutable.of(0, "AB", new QuotesFinder());
        Replacement replacement1 = Replacement.builder().start(1).text("B").build(); // Contained in immutable
        Replacement replacement2 = Replacement.builder().start(2).text("C").build(); // Not contained in immutable
        ReplacementFinder finder = Mockito.mock(ReplacementFinder.class);
        Mockito.when(finder.findStream(Mockito.anyString())).thenReturn(Stream.of(replacement1, replacement2));
        Mockito.when(replacementFinders.stream()).thenReturn(Stream.of(finder));
        Mockito
            .when(immutableFindService.findImmutables(Mockito.anyString()))
            .thenReturn(Collections.singleton(immutable1));

        List<Replacement> replacements = replacementFindService.findReplacements(" ");

        Assert.assertFalse(replacements.isEmpty());
        Assert.assertEquals(1, replacements.size());
        Assert.assertTrue(replacements.contains(replacement2));
    }

    @Test
    public void testFindReplacementsAllIgnored() {
        Immutable immutable1 = Immutable.of(0, "AB", new QuotesFinder());
        Replacement replacement1 = Replacement.builder().start(1).text("B").build();
        ReplacementFinder finder = Mockito.mock(ReplacementFinder.class);
        Mockito.when(finder.findStream(Mockito.anyString())).thenReturn(Stream.of(replacement1));
        Mockito.when(replacementFinders.stream()).thenReturn(Stream.of(finder));
        Mockito
            .when(immutableFindService.findImmutables(Mockito.anyString()))
            .thenReturn(Collections.singleton(immutable1));

        List<Replacement> replacements = replacementFindService.findReplacements("");

        Assert.assertTrue(replacements.isEmpty());
    }

    @Test
    public void testFindCustomReplacements() {
        List<Replacement> replacements = replacementFindService.findCustomReplacements("A X C", "X", "Y");

        Assert.assertFalse(replacements.isEmpty());
        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals("X", replacements.get(0).getText());
    }

    @Test
    public void testFindCustomReplacementsWithNoResults() {
        List<Replacement> replacements = replacementFindService.findCustomReplacements("AXC", "X", "Y");

        Assert.assertTrue(replacements.isEmpty());
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

        Assert.assertFalse(result1.contains(result1));
        Assert.assertFalse(result2.contains(result2));
        Replacement result3 = Replacement.builder().start(1).text("B").build();
        Assert.assertFalse(result1.contains(result3));
        Assert.assertTrue(result2.contains(result3));
        Replacement result4 = Replacement.builder().start(0).text("AB").build();
        Assert.assertFalse(result1.contains(result4));
        Assert.assertFalse(result2.contains(result4));
        Replacement result5 = Replacement.builder().start(0).text("ABC").build();
        Assert.assertFalse(result1.contains(result5));
        Assert.assertFalse(result2.contains(result5));
        Replacement result6 = Replacement.builder().start(2).text("C").build();
        Assert.assertFalse(result1.contains(result6));
        Assert.assertTrue(result2.contains(result6));
    }
}
