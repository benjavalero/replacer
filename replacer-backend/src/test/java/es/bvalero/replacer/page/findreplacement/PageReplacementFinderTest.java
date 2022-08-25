package es.bvalero.replacer.page.findreplacement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.common.domain.Immutable;
import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.finder.immutable.ImmutableFinderService;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PageReplacementFinderTest {

    @Mock
    private ReplacementFinderService replacementFinderService;

    @Mock
    private ImmutableFinderService immutableFinderService;

    @InjectMocks
    private PageReplacementFinder pageReplacementFinder;

    @BeforeEach
    public void setUp() {
        pageReplacementFinder = new PageReplacementFinder();
        MockitoAnnotations.openMocks(this);
    }

    private WikipediaPage buildFakePage(String text) {
        return WikipediaPage
            .builder()
            .id(WikipediaPageId.of(WikipediaLanguage.getDefault(), 1))
            .namespace(WikipediaNamespace.getDefault())
            .title("T")
            .content(text)
            .lastUpdate(LocalDateTime.now())
            .build();
    }

    @Test
    void testEmpty() {
        String text = "An example.";

        when(replacementFinderService.find(any(WikipediaPage.class))).thenReturn(Collections.emptySet());

        Collection<Replacement> replacements = pageReplacementFinder.findReplacements(buildFakePage(text));

        assertTrue(replacements.isEmpty());
    }

    @Test
    void testOneReplacement() {
        String text = "An example.";

        Replacement replacement = Replacement
            .builder()
            .start(0)
            .text("An")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "an"))
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        when(replacementFinderService.find(any(WikipediaPage.class))).thenReturn(new HashSet<>(List.of(replacement)));

        Collection<Replacement> replacements = pageReplacementFinder.findReplacements(buildFakePage(text));

        Collection<Replacement> expected = List.of(replacement);
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testTwoReplacements() {
        String text = "An example.";

        Replacement replacement1 = Replacement
            .builder()
            .start(0)
            .text("An")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "an"))
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement
            .builder()
            .start(3)
            .text("example")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "example"))
            .suggestions(List.of(Suggestion.ofNoComment("ejemplo")))
            .build();
        when(replacementFinderService.find(any(WikipediaPage.class)))
            .thenReturn(new HashSet<>(List.of(replacement1, replacement2)));

        Collection<Replacement> replacements = pageReplacementFinder.findReplacements(buildFakePage(text));

        Collection<Replacement> expected = List.of(replacement1, replacement2);
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testEqualReplacements() {
        String text = "An example.";

        Replacement replacement1 = Replacement
            .builder()
            .start(0)
            .text("An")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "an"))
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement
            .builder()
            .start(0)
            .text("An")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "an"))
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        assertEquals(replacement1, replacement2);

        // We cannot use Set.of because it fails if there are duplicates
        Set<Replacement> found = new HashSet<>(List.of(replacement1, replacement2));
        assertEquals(1, found.size());
        assertEquals(Set.of(replacement1), found);

        when(replacementFinderService.find(any(WikipediaPage.class))).thenReturn(found);

        Collection<Replacement> replacements = pageReplacementFinder.findReplacements(buildFakePage(text));

        assertEquals(new HashSet<>(found), new HashSet<>(replacements));
    }

    @Test
    void testNestedReplacements() {
        String text = "An example.";

        Replacement replacement1 = Replacement
            .builder()
            .start(0)
            .text("An")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "an"))
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement
            .builder()
            .start(0)
            .text("An example")
            .type(ReplacementType.of(ReplacementKind.COMPOSED, "an example"))
            .suggestions(List.of(Suggestion.ofNoComment("Un ejemplo")))
            .build();
        assertTrue(replacement2.containsStrictly(replacement1));

        when(replacementFinderService.find(any(WikipediaPage.class)))
            .thenReturn(new HashSet<>(List.of(replacement1, replacement2)));

        Collection<Replacement> replacements = pageReplacementFinder.findReplacements(buildFakePage(text));

        Collection<Replacement> expected = List.of(replacement2);
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testImmutableNotIntersecting() {
        String text = "An example or two.";

        Replacement replacement1 = Replacement
            .builder()
            .start(0)
            .text("An")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "an"))
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement
            .builder()
            .start(3)
            .text("example")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "example"))
            .suggestions(List.of(Suggestion.ofNoComment("ejemplo")))
            .build();
        Immutable immutable = Immutable.of(14, "two");

        when(replacementFinderService.find(any(WikipediaPage.class)))
            .thenReturn(new HashSet<>(List.of(replacement1, replacement2)));
        when(immutableFinderService.findIterable(any(WikipediaPage.class))).thenReturn(List.of(immutable));

        Collection<Replacement> replacements = pageReplacementFinder.findReplacements(buildFakePage(text));

        Collection<Replacement> expected = List.of(replacement1, replacement2);
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testImmutableEquals() {
        String text = "An example or two.";

        Replacement replacement1 = Replacement
            .builder()
            .start(0)
            .text("An")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "an"))
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement
            .builder()
            .start(3)
            .text("example")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "an"))
            .suggestions(List.of(Suggestion.ofNoComment("ejemplo")))
            .build();
        Immutable immutable = Immutable.of(0, "An");

        when(replacementFinderService.find(any(WikipediaPage.class)))
            .thenReturn(new HashSet<>(List.of(replacement1, replacement2)));
        when(immutableFinderService.findIterable(any(WikipediaPage.class))).thenReturn(List.of(immutable));

        Collection<Replacement> replacements = pageReplacementFinder.findReplacements(buildFakePage(text));

        Collection<Replacement> expected = List.of(replacement2);
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testImmutableGreater() {
        String text = "An example or two.";

        Replacement replacement1 = Replacement
            .builder()
            .start(0)
            .text("An")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "an"))
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement
            .builder()
            .start(3)
            .text("example")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "ejemplo"))
            .suggestions(List.of(Suggestion.ofNoComment("ejemplo")))
            .build();
        Immutable immutable = Immutable.of(0, "An example");

        when(replacementFinderService.find(any(WikipediaPage.class)))
            .thenReturn(new HashSet<>(List.of(replacement1, replacement2)));
        when(immutableFinderService.findIterable(any(WikipediaPage.class))).thenReturn(List.of(immutable));

        Collection<Replacement> replacements = pageReplacementFinder.findReplacements(buildFakePage(text));

        assertEquals(Collections.emptySet(), new HashSet<>(replacements));
    }

    @Test
    void testImmutableIntersects() {
        String text = "An example or two.";

        Replacement replacement = Replacement
            .builder()
            .start(0)
            .text("An example")
            .type(ReplacementType.of(ReplacementKind.COMPOSED, "an example"))
            .suggestions(List.of(Suggestion.ofNoComment("Un ejemplo")))
            .build();
        Immutable immutable = Immutable.of(3, "example or");

        when(replacementFinderService.find(any(WikipediaPage.class))).thenReturn(new HashSet<>(List.of(replacement)));
        when(immutableFinderService.findIterable(any(WikipediaPage.class))).thenReturn(List.of(immutable));

        Collection<Replacement> replacements = pageReplacementFinder.findReplacements(buildFakePage(text));

        Collection<Replacement> expected = List.of(replacement);
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testContains() {
        Replacement result1 = Replacement
            .builder()
            .start(0)
            .text("A")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "a"))
            .suggestions(List.of(Suggestion.ofNoComment("x")))
            .build();
        Replacement result2 = Replacement
            .builder()
            .start(1)
            .text("BC")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "a"))
            .suggestions(List.of(Suggestion.ofNoComment("x")))
            .build();
        Replacement result3 = Replacement
            .builder()
            .start(1)
            .text("B")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "a"))
            .suggestions(List.of(Suggestion.ofNoComment("x")))
            .build();
        Replacement result4 = Replacement
            .builder()
            .start(0)
            .text("AB")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "a"))
            .suggestions(List.of(Suggestion.ofNoComment("x")))
            .build();
        Replacement result5 = Replacement
            .builder()
            .start(0)
            .text("ABC")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "a"))
            .suggestions(List.of(Suggestion.ofNoComment("x")))
            .build();
        Replacement result6 = Replacement
            .builder()
            .start(2)
            .text("C")
            .type(ReplacementType.of(ReplacementKind.SIMPLE, "a"))
            .suggestions(List.of(Suggestion.ofNoComment("x")))
            .build();

        assertTrue(result1.contains(result1));
        assertTrue(result1.intersects(result1));
        assertFalse(result1.containsStrictly(result1));

        assertTrue(result2.intersects(result3));
        assertTrue(result2.contains(result3));
        assertTrue(result2.containsStrictly(result3));
        assertTrue(result3.intersects(result3));
        assertFalse(result3.contains(result2));
        assertFalse(result3.containsStrictly(result2));

        assertTrue(result2.intersects(result4));
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
