package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.finder.common.FinderPage;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinderService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class ReplacementFinderServiceTest {

    @Mock
    private List<ReplacementFinder> replacementFinders;

    @Mock
    private ImmutableFinderService immutableFinderService;

    @InjectMocks
    private ReplacementFinderService replacementFinderService;

    @BeforeEach
    public void setUp() {
        replacementFinderService = new ReplacementFinderService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testFindReplacementsEmpty() {
        Mockito.when(replacementFinders.toArray()).thenReturn(new ReplacementFinder[] {});
        Assertions.assertTrue(replacementFinderService.findList("").isEmpty());
    }

    @Test
    void testFindReplacements() {
        Replacement replacement = Replacement.builder().start(0).text("").build();
        ReplacementFinder finder = Mockito.mock(ReplacementFinder.class);
        Mockito.when(finder.find(Mockito.any(FinderPage.class))).thenReturn(List.of(replacement));
        Mockito.when(replacementFinders.toArray()).thenReturn(new ReplacementFinder[] { finder });

        List<Replacement> replacements = replacementFinderService.findList(" ");

        Assertions.assertFalse(replacements.isEmpty());
        Assertions.assertEquals(1, replacements.size());
        Assertions.assertTrue(replacements.contains(replacement));
    }

    @Test
    void testFindReplacementsIgnoringImmutables() {
        Immutable immutable1 = Immutable.of(0, "AB");
        Replacement replacement1 = Replacement.builder().start(1).text("B").build(); // Contained in immutable
        Replacement replacement2 = Replacement.builder().start(2).text("C").build(); // Not contained in immutable
        ReplacementFinder finder = Mockito.mock(ReplacementFinder.class);
        Mockito.when(finder.find(Mockito.any(FinderPage.class))).thenReturn(List.of(replacement1, replacement2));
        Mockito.when(replacementFinders.toArray()).thenReturn(new ReplacementFinder[] { finder });
        Mockito
            .when(immutableFinderService.find(Mockito.any(FinderPage.class)))
            .thenReturn(Collections.singleton(immutable1));

        List<Replacement> replacements = replacementFinderService.findList(" ");

        Assertions.assertFalse(replacements.isEmpty());
        Assertions.assertEquals(1, replacements.size());
        Assertions.assertTrue(replacements.contains(replacement2));
    }

    @Test
    void testFindReplacementsAllIgnored() {
        Immutable immutable1 = Immutable.of(0, "AB");
        Replacement replacement1 = Replacement.builder().start(1).text("B").build();
        ReplacementFinder finder = Mockito.mock(ReplacementFinder.class);
        Mockito.when(finder.find(Mockito.any(FinderPage.class))).thenReturn(List.of(replacement1));
        Mockito.when(replacementFinders.toArray()).thenReturn(new ReplacementFinder[] { finder });
        Mockito
            .when(immutableFinderService.find(Mockito.any(FinderPage.class)))
            .thenReturn(Collections.singleton(immutable1));

        List<Replacement> replacements = replacementFinderService.findList("");

        Assertions.assertTrue(replacements.isEmpty());
    }

    @Test
    void testComparePageReplacements() {
        Replacement result1 = Replacement.builder().start(0).text("A").build();
        Replacement result2 = Replacement.builder().start(0).text("AB").build();
        Replacement result3 = Replacement.builder().start(1).text("BC").build();
        Replacement result4 = Replacement.builder().start(1).text("BCD").build();
        Replacement result5 = Replacement.builder().start(2).text("C").build();
        List<Replacement> results = Arrays.asList(result1, result2, result3, result4, result5);

        Collections.sort(results);

        // Order descendant by start. If equals, the lower end.
        Assertions.assertEquals(result5, results.get(0));
        Assertions.assertEquals(result3, results.get(1));
        Assertions.assertEquals(result4, results.get(2));
        Assertions.assertEquals(result1, results.get(3));
        Assertions.assertEquals(result2, results.get(4));
    }

    @Test
    void testIsContained() {
        Replacement result1 = Replacement.builder().start(0).text("A").build();
        Replacement result2 = Replacement.builder().start(1).text("BC").build();

        Assertions.assertFalse(result1.contains(result1));
        Assertions.assertFalse(result2.contains(result2));
        Replacement result3 = Replacement.builder().start(1).text("B").build();
        Assertions.assertFalse(result1.contains(result3));
        Assertions.assertTrue(result2.contains(result3));
        Replacement result4 = Replacement.builder().start(0).text("AB").build();
        Assertions.assertFalse(result1.contains(result4));
        Assertions.assertFalse(result2.contains(result4));
        Replacement result5 = Replacement.builder().start(0).text("ABC").build();
        Assertions.assertFalse(result1.contains(result5));
        Assertions.assertFalse(result2.contains(result5));
        Replacement result6 = Replacement.builder().start(2).text("C").build();
        Assertions.assertFalse(result1.contains(result6));
        Assertions.assertTrue(result2.contains(result6));
    }
}
