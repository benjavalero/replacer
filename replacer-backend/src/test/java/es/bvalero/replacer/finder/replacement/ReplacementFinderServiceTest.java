package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.finder.common.FinderPage;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinderService;
import java.util.*;
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
    void testEmpty() {
        String text = "An example.";

        ReplacementFinder replacementFinder = Mockito.mock(ReplacementFinder.class);
        Mockito.when(replacementFinder.find(Mockito.any(FinderPage.class))).thenReturn(Collections.emptyList());
        Mockito.when(replacementFinders.toArray()).thenReturn(List.of(replacementFinder).toArray());

        List<Replacement> replacements = replacementFinderService.findList(text);

        Assertions.assertTrue(replacements.isEmpty());
    }

    @Test
    void testOneReplacement() {
        String text = "An example.";

        Replacement replacement = Replacement.builder().start(0).text("An").build();
        ReplacementFinder replacementFinder = Mockito.mock(ReplacementFinder.class);
        Mockito.when(replacementFinder.find(Mockito.any(FinderPage.class))).thenReturn(List.of(replacement));
        Mockito.when(replacementFinders.toArray()).thenReturn(List.of(replacementFinder).toArray());

        List<Replacement> replacements = replacementFinderService.findList(text);

        Assertions.assertEquals(Set.of(replacement), new HashSet<>(replacements));
    }

    @Test
    void testTwoReplacements() {
        String text = "An example.";

        Replacement replacement1 = Replacement.builder().start(0).text("An").build();
        Replacement replacement2 = Replacement.builder().start(3).text("example").build();
        ReplacementFinder replacementFinder = Mockito.mock(ReplacementFinder.class);
        Mockito
            .when(replacementFinder.find(Mockito.any(FinderPage.class)))
            .thenReturn(List.of(replacement1, replacement2));
        Mockito.when(replacementFinders.toArray()).thenReturn(List.of(replacementFinder).toArray());

        List<Replacement> replacements = replacementFinderService.findList(text);

        Assertions.assertEquals(Set.of(replacement1, replacement2), new HashSet<>(replacements));
    }

    @Test
    void testEqualReplacements() {
        String text = "An example.";

        Replacement replacement1 = Replacement.builder().start(0).text("An").build();
        Replacement replacement2 = Replacement.builder().start(0).text("An").build();
        Assertions.assertEquals(replacement1, replacement2);

        ReplacementFinder replacementFinder1 = Mockito.mock(ReplacementFinder.class);
        ReplacementFinder replacementFinder2 = Mockito.mock(ReplacementFinder.class);
        Mockito.when(replacementFinder1.find(Mockito.any(FinderPage.class))).thenReturn(List.of(replacement1));
        Mockito.when(replacementFinder2.find(Mockito.any(FinderPage.class))).thenReturn(List.of(replacement2));
        Mockito
            .when(replacementFinders.toArray())
            .thenReturn(List.of(replacementFinder1, replacementFinder2).toArray());

        List<Replacement> replacements = replacementFinderService.findList(text);

        Assertions.assertEquals(Set.of(replacement1), new HashSet<>(replacements));
    }

    @Test
    void testNestedReplacements() {
        String text = "An example.";

        Replacement replacement1 = Replacement.builder().start(0).text("An").build();
        Replacement replacement2 = Replacement.builder().start(0).text("An example").build();
        Assertions.assertTrue(replacement2.containsStrictly(replacement1));

        ReplacementFinder replacementFinder1 = Mockito.mock(ReplacementFinder.class);
        ReplacementFinder replacementFinder2 = Mockito.mock(ReplacementFinder.class);
        Mockito.when(replacementFinder1.find(Mockito.any(FinderPage.class))).thenReturn(List.of(replacement1));
        Mockito.when(replacementFinder2.find(Mockito.any(FinderPage.class))).thenReturn(List.of(replacement2));
        Mockito
            .when(replacementFinders.toArray())
            .thenReturn(List.of(replacementFinder1, replacementFinder2).toArray());

        List<Replacement> replacements = replacementFinderService.findList(text);

        Assertions.assertEquals(Set.of(replacement2), new HashSet<>(replacements));
    }

    @Test
    void testImmutableNotIntersecting() {
        String text = "An example or two.";

        Replacement replacement1 = Replacement.builder().start(0).text("An").build();
        Replacement replacement2 = Replacement.builder().start(3).text("example").build();
        Immutable immutable = Immutable.of(14, "two");
        ReplacementFinder replacementFinder = Mockito.mock(ReplacementFinder.class);
        Mockito
            .when(replacementFinder.find(Mockito.any(FinderPage.class)))
            .thenReturn(List.of(replacement1, replacement2));
        Mockito.when(replacementFinders.toArray()).thenReturn(List.of(replacementFinder).toArray());
        Mockito.when(immutableFinderService.find(Mockito.any(FinderPage.class))).thenReturn(List.of(immutable));

        List<Replacement> replacements = replacementFinderService.findList(text);

        Assertions.assertEquals(Set.of(replacement1, replacement2), new HashSet<>(replacements));
    }

    @Test
    void testImmutableEquals() {
        String text = "An example or two.";

        Replacement replacement1 = Replacement.builder().start(0).text("An").build();
        Replacement replacement2 = Replacement.builder().start(3).text("example").build();
        Immutable immutable = Immutable.of(0, "An");
        ReplacementFinder replacementFinder = Mockito.mock(ReplacementFinder.class);
        Mockito
            .when(replacementFinder.find(Mockito.any(FinderPage.class)))
            .thenReturn(List.of(replacement1, replacement2));
        Mockito.when(replacementFinders.toArray()).thenReturn(List.of(replacementFinder).toArray());
        Mockito.when(immutableFinderService.find(Mockito.any(FinderPage.class))).thenReturn(List.of(immutable));

        List<Replacement> replacements = replacementFinderService.findList(text);

        Assertions.assertEquals(Set.of(replacement2), new HashSet<>(replacements));
    }

    @Test
    void testImmutableGreater() {
        String text = "An example or two.";

        Replacement replacement1 = Replacement.builder().start(0).text("An").build();
        Replacement replacement2 = Replacement.builder().start(3).text("example").build();
        Immutable immutable = Immutable.of(0, "An example");
        ReplacementFinder replacementFinder = Mockito.mock(ReplacementFinder.class);
        Mockito
            .when(replacementFinder.find(Mockito.any(FinderPage.class)))
            .thenReturn(List.of(replacement1, replacement2));
        Mockito.when(replacementFinders.toArray()).thenReturn(List.of(replacementFinder).toArray());
        Mockito.when(immutableFinderService.find(Mockito.any(FinderPage.class))).thenReturn(List.of(immutable));

        List<Replacement> replacements = replacementFinderService.findList(text);

        Assertions.assertEquals(Collections.emptySet(), new HashSet<>(replacements));
    }

    @Test
    void testImmutableIntersects() {
        String text = "An example or two.";

        Replacement replacement = Replacement.builder().start(0).text("An example").build();
        Immutable immutable = Immutable.of(3, "example or");
        ReplacementFinder replacementFinder = Mockito.mock(ReplacementFinder.class);
        Mockito.when(replacementFinder.find(Mockito.any(FinderPage.class))).thenReturn(List.of(replacement));
        Mockito.when(replacementFinders.toArray()).thenReturn(List.of(replacementFinder).toArray());
        Mockito.when(immutableFinderService.find(Mockito.any(FinderPage.class))).thenReturn(List.of(immutable));

        List<Replacement> replacements = replacementFinderService.findList(text);

        Assertions.assertEquals(Collections.emptySet(), new HashSet<>(replacements));
    }

    @Test
    void testContains() {
        Replacement result1 = Replacement.builder().start(0).text("A").build();
        Replacement result2 = Replacement.builder().start(1).text("BC").build();
        Replacement result3 = Replacement.builder().start(1).text("B").build();
        Replacement result4 = Replacement.builder().start(0).text("AB").build();
        Replacement result5 = Replacement.builder().start(0).text("ABC").build();
        Replacement result6 = Replacement.builder().start(2).text("C").build();

        Assertions.assertTrue(result1.contains(result1));
        Assertions.assertTrue(result1.intersects(result1));
        Assertions.assertFalse(result1.containsStrictly(result1));

        Assertions.assertTrue(result2.intersects(result3));
        Assertions.assertTrue(result2.contains(result3));
        Assertions.assertTrue(result2.containsStrictly(result3));
        Assertions.assertTrue(result3.intersects(result3));
        Assertions.assertFalse(result3.contains(result2));
        Assertions.assertFalse(result3.containsStrictly(result2));

        Assertions.assertTrue(result2.intersects(result4));
        Assertions.assertFalse(result2.contains(result4));
        Assertions.assertFalse(result4.contains(result2));
        Assertions.assertTrue(result4.contains(result1));
        Assertions.assertTrue(result4.contains(result3));

        Assertions.assertTrue(result5.containsStrictly(result1));
        Assertions.assertTrue(result5.containsStrictly(result2));
        Assertions.assertTrue(result5.containsStrictly(result3));
        Assertions.assertTrue(result5.containsStrictly(result4));

        Assertions.assertFalse(result1.containsStrictly(result6));
        Assertions.assertTrue(result2.containsStrictly(result6));
    }
}
