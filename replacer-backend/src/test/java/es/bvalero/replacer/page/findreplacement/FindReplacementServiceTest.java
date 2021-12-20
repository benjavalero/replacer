package es.bvalero.replacer.page.findreplacement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinderService;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.finder.replacement.ReplacementMapper;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class FindReplacementServiceTest {

    @Mock
    private ReplacementFinderService replacementFinderService;

    @Mock
    private ImmutableFinderService immutableFinderService;

    @InjectMocks
    private FindReplacementsService findReplacementsService;

    @BeforeEach
    public void setUp() {
        findReplacementsService = new FindReplacementsService();
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

        when(replacementFinderService.find(any(FinderPage.class))).thenReturn(Collections.emptyList());

        Collection<PageReplacement> replacements = findReplacementsService.findReplacements(buildFakePage(text));

        assertTrue(replacements.isEmpty());
    }

    @Test
    void testOneReplacement() {
        String text = "An example.";

        Replacement replacement = Replacement
            .builder()
            .start(0)
            .text("An")
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        when(replacementFinderService.find(any(FinderPage.class))).thenReturn(List.of(replacement));

        Collection<PageReplacement> replacements = findReplacementsService.findReplacements(buildFakePage(text));

        Collection<PageReplacement> expected = ReplacementMapper.toDomain(List.of(replacement));
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testTwoReplacements() {
        String text = "An example.";

        Replacement replacement1 = Replacement
            .builder()
            .start(0)
            .text("An")
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement
            .builder()
            .start(3)
            .text("example")
            .suggestions(List.of(Suggestion.ofNoComment("ejemplo")))
            .build();
        when(replacementFinderService.find(any(FinderPage.class))).thenReturn(List.of(replacement1, replacement2));

        Collection<PageReplacement> replacements = findReplacementsService.findReplacements(buildFakePage(text));

        Collection<PageReplacement> expected = ReplacementMapper.toDomain(List.of(replacement1, replacement2));
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testEqualReplacements() {
        String text = "An example.";

        Replacement replacement1 = Replacement
            .builder()
            .start(0)
            .text("An")
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement
            .builder()
            .start(0)
            .text("An")
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        assertEquals(replacement1, replacement2);

        when(replacementFinderService.find(any(FinderPage.class))).thenReturn(List.of(replacement1, replacement2));

        Collection<PageReplacement> replacements = findReplacementsService.findReplacements(buildFakePage(text));

        Collection<PageReplacement> expected = ReplacementMapper.toDomain(List.of(replacement1));
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testNestedReplacements() {
        String text = "An example.";

        Replacement replacement1 = Replacement
            .builder()
            .start(0)
            .text("An")
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement
            .builder()
            .start(0)
            .text("An example")
            .suggestions(List.of(Suggestion.ofNoComment("Un ejemplo")))
            .build();
        assertTrue(replacement2.containsStrictly(replacement1));

        when(replacementFinderService.find(any(FinderPage.class))).thenReturn(List.of(replacement1, replacement2));

        Collection<PageReplacement> replacements = findReplacementsService.findReplacements(buildFakePage(text));

        Collection<PageReplacement> expected = ReplacementMapper.toDomain(List.of(replacement2));
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testImmutableNotIntersecting() {
        String text = "An example or two.";

        Replacement replacement1 = Replacement
            .builder()
            .start(0)
            .text("An")
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement
            .builder()
            .start(3)
            .text("example")
            .suggestions(List.of(Suggestion.ofNoComment("ejemplo")))
            .build();
        Immutable immutable = Immutable.of(14, "two");

        when(replacementFinderService.find(any(FinderPage.class))).thenReturn(List.of(replacement1, replacement2));
        when(immutableFinderService.findIterable(any(FinderPage.class))).thenReturn(List.of(immutable));

        Collection<PageReplacement> replacements = findReplacementsService.findReplacements(buildFakePage(text));

        Collection<PageReplacement> expected = ReplacementMapper.toDomain(List.of(replacement1, replacement2));
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testImmutableEquals() {
        String text = "An example or two.";

        Replacement replacement1 = Replacement
            .builder()
            .start(0)
            .text("An")
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement
            .builder()
            .start(3)
            .text("example")
            .suggestions(List.of(Suggestion.ofNoComment("ejemplo")))
            .build();
        Immutable immutable = Immutable.of(0, "An");

        when(replacementFinderService.find(any(FinderPage.class))).thenReturn(List.of(replacement1, replacement2));
        when(immutableFinderService.findIterable(any(FinderPage.class))).thenReturn(List.of(immutable));

        Collection<PageReplacement> replacements = findReplacementsService.findReplacements(buildFakePage(text));

        Collection<PageReplacement> expected = ReplacementMapper.toDomain(List.of(replacement2));
        assertEquals(new HashSet<>(expected), new HashSet<>(replacements));
    }

    @Test
    void testImmutableGreater() {
        String text = "An example or two.";

        Replacement replacement1 = Replacement
            .builder()
            .start(0)
            .text("An")
            .suggestions(List.of(Suggestion.ofNoComment("Un")))
            .build();
        Replacement replacement2 = Replacement
            .builder()
            .start(3)
            .text("example")
            .suggestions(List.of(Suggestion.ofNoComment("ejemplo")))
            .build();
        Immutable immutable = Immutable.of(0, "An example");

        when(replacementFinderService.find(any(FinderPage.class))).thenReturn(List.of(replacement1, replacement2));
        when(immutableFinderService.findIterable(any(FinderPage.class))).thenReturn(List.of(immutable));

        Collection<PageReplacement> replacements = findReplacementsService.findReplacements(buildFakePage(text));

        assertEquals(Collections.emptySet(), new HashSet<>(replacements));
    }

    @Test
    void testImmutableIntersects() {
        String text = "An example or two.";

        Replacement replacement = Replacement
            .builder()
            .start(0)
            .text("An example")
            .suggestions(List.of(Suggestion.ofNoComment("Un ejemplo")))
            .build();
        Immutable immutable = Immutable.of(3, "example or");

        when(replacementFinderService.find(any(FinderPage.class))).thenReturn(List.of(replacement));
        when(immutableFinderService.findIterable(any(FinderPage.class))).thenReturn(List.of(immutable));

        Collection<PageReplacement> replacements = findReplacementsService.findReplacements(buildFakePage(text));

        assertEquals(Collections.emptySet(), new HashSet<>(replacements));
    }

    @Test
    void testContains() {
        Replacement result1 = Replacement.builder().start(0).text("A").build();
        Replacement result2 = Replacement.builder().start(1).text("BC").build();
        Replacement result3 = Replacement.builder().start(1).text("B").build();
        Replacement result4 = Replacement.builder().start(0).text("AB").build();
        Replacement result5 = Replacement.builder().start(0).text("ABC").build();
        Replacement result6 = Replacement.builder().start(2).text("C").build();

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