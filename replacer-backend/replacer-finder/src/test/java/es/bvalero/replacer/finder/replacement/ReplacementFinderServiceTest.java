package es.bvalero.replacer.finder.replacement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.immutable.ImmutableFinderService;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReplacementFinderServiceTest {

    // Dependency injection
    private ReplacementFinder replacementFinder;
    private ImmutableFinderService immutableFinderService;

    private ReplacementFinderApi replacementFinderService;

    @BeforeEach
    public void setUp() {
        replacementFinder = mock(ReplacementFinder.class);
        immutableFinderService = mock(ImmutableFinderService.class);
        replacementFinderService = new ReplacementFinderApi(List.of(replacementFinder), immutableFinderService);
    }

    @Test
    void testEmpty() {
        String text = "An example.";

        when(replacementFinder.find(any(FinderPage.class))).thenReturn(Collections.emptySet());

        Collection<Replacement> replacements = replacementFinderService.findReplacements(FinderPage.of(text));

        assertTrue(replacements.isEmpty());
    }

    @Test
    void testOneReplacement() {
        String text = "An example.";

        Replacement replacement = Replacement.builder()
            .start(0)
            .text("An")
            .type(StandardType.of(ReplacementKind.SIMPLE, "an"))
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        when(replacementFinder.find(any(FinderPage.class))).thenReturn(new HashSet<>(List.of(replacement)));

        Collection<Replacement> replacements = replacementFinderService.findReplacements(FinderPage.of(text));

        Collection<Replacement> expected = List.of(replacement);
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testTwoReplacements() {
        String text = "An example.";

        Replacement replacement1 = Replacement.builder()
            .start(0)
            .text("An")
            .type(StandardType.of(ReplacementKind.SIMPLE, "an"))
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement.builder()
            .start(3)
            .text("example")
            .type(StandardType.of(ReplacementKind.SIMPLE, "example"))
            .suggestions(List.of(Suggestion.ofNoComment("ejemplo")))
            .build();
        when(replacementFinder.find(any(FinderPage.class))).thenReturn(
            new HashSet<>(List.of(replacement1, replacement2))
        );

        Collection<Replacement> replacements = replacementFinderService.findReplacements(FinderPage.of(text));

        Collection<Replacement> expected = List.of(replacement1, replacement2);
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testEqualReplacements() {
        String text = "An example.";

        Replacement replacement1 = Replacement.builder()
            .start(0)
            .text("An")
            .type(StandardType.of(ReplacementKind.SIMPLE, "an"))
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement.builder()
            .start(0)
            .text("An")
            .type(StandardType.of(ReplacementKind.SIMPLE, "an"))
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        assertEquals(replacement1, replacement2);

        // We cannot use Set.of because it fails if there are duplicates
        Set<Replacement> found = new HashSet<>(List.of(replacement1, replacement2));
        assertEquals(1, found.size());
        assertEquals(Set.of(replacement1), found);

        when(replacementFinder.find(any(FinderPage.class))).thenReturn(found);

        Collection<Replacement> replacements = replacementFinderService.findReplacements(FinderPage.of(text));

        assertEquals(new HashSet<>(found), new HashSet<>(replacements));
    }

    @Test
    void testNestedReplacements() {
        String text = "An example.";

        Replacement replacement1 = Replacement.builder()
            .start(0)
            .text("An")
            .type(StandardType.of(ReplacementKind.SIMPLE, "an"))
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement.builder()
            .start(0)
            .text("An example")
            .type(StandardType.of(ReplacementKind.COMPOSED, "an example"))
            .suggestions(List.of(Suggestion.ofNoComment("Un ejemplo")))
            .build();
        assertTrue(replacement2.containsStrictly(replacement1));

        when(replacementFinder.find(any(FinderPage.class))).thenReturn(
            new HashSet<>(List.of(replacement1, replacement2))
        );

        Collection<Replacement> replacements = replacementFinderService.findReplacements(FinderPage.of(text));

        Collection<Replacement> expected = List.of(replacement2);
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testImmutableNotIntersecting() {
        String text = "An example or two.";

        Replacement replacement1 = Replacement.builder()
            .start(0)
            .text("An")
            .type(StandardType.of(ReplacementKind.SIMPLE, "an"))
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement.builder()
            .start(3)
            .text("example")
            .type(StandardType.of(ReplacementKind.SIMPLE, "example"))
            .suggestions(List.of(Suggestion.ofNoComment("ejemplo")))
            .build();
        Immutable immutable = Immutable.of(14, "two");

        when(replacementFinder.find(any(FinderPage.class))).thenReturn(
            new HashSet<>(List.of(replacement1, replacement2))
        );
        when(immutableFinderService.findIterable(any(FinderPage.class))).thenReturn(List.of(immutable));

        Collection<Replacement> replacements = replacementFinderService.findReplacements(FinderPage.of(text));

        Collection<Replacement> expected = List.of(replacement1, replacement2);
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testImmutableEquals() {
        String text = "An example or two.";

        Replacement replacement1 = Replacement.builder()
            .start(0)
            .text("An")
            .type(StandardType.of(ReplacementKind.SIMPLE, "an"))
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement.builder()
            .start(3)
            .text("example")
            .type(StandardType.of(ReplacementKind.SIMPLE, "example"))
            .suggestions(List.of(Suggestion.ofNoComment("ejemplo")))
            .build();
        Immutable immutable = Immutable.of(0, "An");

        when(replacementFinder.find(any(FinderPage.class))).thenReturn(
            new HashSet<>(List.of(replacement1, replacement2))
        );
        when(immutableFinderService.findIterable(any(FinderPage.class))).thenReturn(List.of(immutable));

        Collection<Replacement> replacements = replacementFinderService.findReplacements(FinderPage.of(text));

        Collection<Replacement> expected = List.of(replacement2);
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testImmutableGreater() {
        String text = "An example or two.";

        Replacement replacement1 = Replacement.builder()
            .start(0)
            .text("An")
            .type(StandardType.of(ReplacementKind.SIMPLE, "an"))
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement.builder()
            .start(3)
            .text("example")
            .type(StandardType.of(ReplacementKind.SIMPLE, "ejemplo"))
            .suggestions(List.of(Suggestion.ofNoComment("ejemplo")))
            .build();
        Immutable immutable = Immutable.of(0, "An example");

        when(replacementFinder.find(any(FinderPage.class))).thenReturn(
            new HashSet<>(List.of(replacement1, replacement2))
        );
        when(immutableFinderService.findIterable(any(FinderPage.class))).thenReturn(List.of(immutable));

        Collection<Replacement> replacements = replacementFinderService.findReplacements(FinderPage.of(text));

        assertEquals(Set.of(), new HashSet<>(replacements));
    }

    @Test
    void testContains() {
        Replacement result1 = Replacement.builder()
            .start(0)
            .text("A")
            .type(StandardType.of(ReplacementKind.SIMPLE, "a"))
            .suggestions(List.of(Suggestion.ofNoComment("x")))
            .build();
        Replacement result2 = Replacement.builder()
            .start(1)
            .text("BC")
            .type(StandardType.of(ReplacementKind.SIMPLE, "a"))
            .suggestions(List.of(Suggestion.ofNoComment("x")))
            .build();
        Replacement result3 = Replacement.builder()
            .start(1)
            .text("B")
            .type(StandardType.of(ReplacementKind.SIMPLE, "a"))
            .suggestions(List.of(Suggestion.ofNoComment("x")))
            .build();
        Replacement result4 = Replacement.builder()
            .start(0)
            .text("AB")
            .type(StandardType.of(ReplacementKind.SIMPLE, "a"))
            .suggestions(List.of(Suggestion.ofNoComment("x")))
            .build();
        Replacement result5 = Replacement.builder()
            .start(0)
            .text("ABC")
            .type(StandardType.of(ReplacementKind.SIMPLE, "a"))
            .suggestions(List.of(Suggestion.ofNoComment("x")))
            .build();
        Replacement result6 = Replacement.builder()
            .start(2)
            .text("C")
            .type(StandardType.of(ReplacementKind.SIMPLE, "a"))
            .suggestions(List.of(Suggestion.ofNoComment("x")))
            .build();

        assertTrue(result1.contains(result1));
        assertFalse(result1.containsStrictly(result1));

        assertTrue(result2.contains(result3));
        assertTrue(result2.containsStrictly(result3));
        assertFalse(result3.contains(result2));
        assertFalse(result3.containsStrictly(result2));

        assertFalse(result2.contains(result4));
        assertFalse(result4.contains(result2));
        assertTrue(result4.contains(result1));
        assertTrue(result4.contains(result3));

        assertTrue(result5.containsStrictly(result1));
        assertTrue(result5.containsStrictly(result2));
        assertTrue(result5.containsStrictly(result3));
        assertTrue(result5.containsStrictly(result4));

        assertFalse(result1.containsStrictly(result6));
        assertTrue(result2.containsStrictly(result6));
    }
}
