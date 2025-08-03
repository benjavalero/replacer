package es.bvalero.replacer.finder.replacement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.finder.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReplacementFinderServiceTest {

    // Dependency injection
    private ReplacementFinder replacementFinder;
    private ImmutableFindApi immutableFindApi;

    private ReplacementFinderService replacementFinderService;

    @BeforeEach
    public void setUp() {
        replacementFinder = mock(ReplacementFinder.class);
        immutableFindApi = mock(ImmutableFindApi.class);
        replacementFinderService = new ReplacementFinderService(List.of(replacementFinder), immutableFindApi);
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

        Replacement replacement = Replacement.of(
            0,
            "An",
            StandardType.of(ReplacementKind.SIMPLE, "an"),
            List.of(Suggestion.ofNoComment("Un")),
            text
        );
        when(replacementFinder.find(any(FinderPage.class))).thenReturn(new HashSet<>(List.of(replacement)));

        Collection<Replacement> replacements = replacementFinderService.findReplacements(FinderPage.of(text));

        Collection<Replacement> expected = List.of(replacement);
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testTwoReplacements() {
        String text = "An example.";

        Replacement replacement1 = Replacement.of(
            0,
            "An",
            StandardType.of(ReplacementKind.SIMPLE, "an"),
            List.of(Suggestion.ofNoComment("Un")),
            text
        );
        Replacement replacement2 = Replacement.of(
            3,
            "example",
            StandardType.of(ReplacementKind.SIMPLE, "example"),
            List.of(Suggestion.ofNoComment("ejemplo")),
            text
        );
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

        Replacement replacement1 = Replacement.of(
            0,
            "An",
            StandardType.of(ReplacementKind.SIMPLE, "an"),
            List.of(Suggestion.ofNoComment("Un")),
            text
        );
        Replacement replacement2 = Replacement.of(
            0,
            "An",
            StandardType.of(ReplacementKind.SIMPLE, "an"),
            List.of(Suggestion.ofNoComment("Un")),
            text
        );
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

        Replacement replacement1 = Replacement.of(
            0,
            "An",
            StandardType.of(ReplacementKind.SIMPLE, "an"),
            List.of(Suggestion.ofNoComment("Un")),
            text
        );
        Replacement replacement2 = Replacement.of(
            0,
            "An example",
            StandardType.of(ReplacementKind.COMPOSED, "an example"),
            List.of(Suggestion.ofNoComment("Un ejemplo")),
            text
        );
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

        Replacement replacement1 = Replacement.of(
            0,
            "An",
            StandardType.of(ReplacementKind.SIMPLE, "an"),
            List.of(Suggestion.ofNoComment("Un")),
            text
        );
        Replacement replacement2 = Replacement.of(
            3,
            "example",
            StandardType.of(ReplacementKind.SIMPLE, "example"),
            List.of(Suggestion.ofNoComment("ejemplo")),
            text
        );
        Immutable immutable = Immutable.of(14, "two");

        when(replacementFinder.find(any(FinderPage.class))).thenReturn(
            new HashSet<>(List.of(replacement1, replacement2))
        );
        when(immutableFindApi.findImmutables(any(FinderPage.class))).thenReturn(List.of(immutable));

        Collection<Replacement> replacements = replacementFinderService.findReplacements(FinderPage.of(text));

        Collection<Replacement> expected = List.of(replacement1, replacement2);
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testImmutableEquals() {
        String text = "An example or two.";

        Replacement replacement1 = Replacement.of(
            0,
            "An",
            StandardType.of(ReplacementKind.SIMPLE, "an"),
            List.of(Suggestion.ofNoComment("Un")),
            text
        );
        Replacement replacement2 = Replacement.of(
            3,
            "example",
            StandardType.of(ReplacementKind.SIMPLE, "example"),
            List.of(Suggestion.ofNoComment("ejemplo")),
            text
        );
        Immutable immutable = Immutable.of(0, "An");

        when(replacementFinder.find(any(FinderPage.class))).thenReturn(
            new HashSet<>(List.of(replacement1, replacement2))
        );
        when(immutableFindApi.findImmutables(any(FinderPage.class))).thenReturn(List.of(immutable));

        Collection<Replacement> replacements = replacementFinderService.findReplacements(FinderPage.of(text));

        Collection<Replacement> expected = List.of(replacement2);
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testImmutableGreater() {
        String text = "An example or two.";

        Replacement replacement1 = Replacement.of(
            0,
            "An",
            StandardType.of(ReplacementKind.SIMPLE, "an"),
            List.of(Suggestion.ofNoComment("Un")),
            text
        );
        Replacement replacement2 = Replacement.of(
            3,
            "example",
            StandardType.of(ReplacementKind.SIMPLE, "ejemplo"),
            List.of(Suggestion.ofNoComment("ejemplo")),
            text
        );
        Immutable immutable = Immutable.of(0, "An example");

        when(replacementFinder.find(any(FinderPage.class))).thenReturn(
            new HashSet<>(List.of(replacement1, replacement2))
        );
        when(immutableFindApi.findImmutables(any(FinderPage.class))).thenReturn(List.of(immutable));

        Collection<Replacement> replacements = replacementFinderService.findReplacements(FinderPage.of(text));

        assertEquals(Set.of(), new HashSet<>(replacements));
    }

    @Test
    void testContains() {
        String text = "ABCDE";
        Replacement result1 = Replacement.of(
            0,
            "A",
            StandardType.of(ReplacementKind.SIMPLE, "a"),
            List.of(Suggestion.ofNoComment("x")),
            text
        );
        Replacement result2 = Replacement.of(
            1,
            "BC",
            StandardType.of(ReplacementKind.SIMPLE, "bc"),
            List.of(Suggestion.ofNoComment("x")),
            text
        );
        Replacement result3 = Replacement.of(
            1,
            "B",
            StandardType.of(ReplacementKind.SIMPLE, "b"),
            List.of(Suggestion.ofNoComment("x")),
            text
        );
        Replacement result4 = Replacement.of(
            0,
            "AB",
            StandardType.of(ReplacementKind.SIMPLE, "ab"),
            List.of(Suggestion.ofNoComment("x")),
            text
        );
        Replacement result5 = Replacement.of(
            0,
            "ABC",
            StandardType.of(ReplacementKind.SIMPLE, "abc"),
            List.of(Suggestion.ofNoComment("x")),
            text
        );
        Replacement result6 = Replacement.of(
            2,
            "C",
            StandardType.of(ReplacementKind.SIMPLE, "c"),
            List.of(Suggestion.ofNoComment("x")),
            text
        );

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
