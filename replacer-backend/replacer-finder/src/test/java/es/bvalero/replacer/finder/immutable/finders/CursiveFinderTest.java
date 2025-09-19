package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Immutable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CursiveFinderTest {

    private CursiveFinder cursiveFinder;

    @BeforeEach
    public void setUp() {
        cursiveFinder = spy(new CursiveFinder());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "''cursive''",
            "'''bold'''",
            "''Beefeater's cool''", // Cursive with quote
            "''A '''Game''' of Thrones''", // Cursive with bold
            "'''A ''little'' bit'''", // Bold with cursive
            "''B''", // Cursive with one character
            "''Be''", // Cursive with two characters
        }
    )
    void testCursive(String cursive) {
        String text = String.format("A %s.", cursive);

        List<Immutable> matches = cursiveFinder.findList(text);

        assertEquals(1, matches.size());
        assertEquals(cursive, matches.get(0).text());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "A '''", // Only open quotes
            "A ''cursive not closed at the end of the text",
            "A '''bold'' not closed.", // Open and close quotes don't match
            "A '' ''.", // Blank
            "A ''\n", // Blank with new line
        }
    )
    void testCursiveNotValid(String text) {
        List<Immutable> matches = cursiveFinder.findList(text);

        assertTrue(matches.isEmpty());
        verify(cursiveFinder, atLeastOnce()).logImmutableCheck(any(FinderPage.class), anyInt(), anyInt(), anyString());
    }

    @Test
    void testCursiveTruncatedWithNewLine() {
        String cursive = "''cursive";
        String text = String.format("A %s\nMore text", cursive);

        List<Immutable> matches = cursiveFinder.findList(text);

        assertEquals(1, matches.size());
        assertEquals(cursive, matches.get(0).text());
    }

    @Test
    void testCursiveAtTheEnd() {
        String cursive = "''cursive''";
        String text = String.format("A %s", cursive);

        List<Immutable> matches = cursiveFinder.findList(text);

        assertEquals(1, matches.size());
        assertEquals(cursive, matches.get(0).text());
    }

    @Test
    void testSeveralCursive() {
        String cursive = "''cursive1''";
        String bold = "'''bold'''";
        String text = String.format("A %s and %s.", cursive, bold);

        List<Immutable> matches = cursiveFinder.findList(text);

        Set<String> expected = Set.of(cursive, bold);
        Set<String> actual = matches.stream().map(Immutable::text).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void testSeveralCursiveTruncated() {
        String cursive1 = "''xxx''";
        String cursive2 = "''yyy"; // Truncated with new line
        String cursive3 = "''zzz''";
        String cursive4 = "''aaa"; // Truncated with end
        String text = String.format("%s %s\n %s %s.", cursive1, cursive2, cursive3, cursive4);

        List<Immutable> matches = cursiveFinder.findList(text);

        Set<String> expected = Set.of(cursive1, cursive2, cursive3);
        Set<String> actual = matches.stream().map(Immutable::text).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void testCursiveWithNestedQuotes() {
        String text =
            "La leyenda: ''Y \"será más terrorífica que 'La Mujer de Judas'\"''. La producción será ''[[Almas en Pena]]''.";

        List<Immutable> matches = cursiveFinder.findList(text);

        Set<String> expected = Set.of(
            "''Y \"será más terrorífica que 'La Mujer de Judas'\"''",
            "''[[Almas en Pena]]''"
        );
        Set<String> actual = matches.stream().map(Immutable::text).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }
}
