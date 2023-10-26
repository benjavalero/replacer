package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CursiveFinderTest {

    @ParameterizedTest
    @ValueSource(
        strings = {
            "''cursive''",
            "'''bold'''",
            // Cursive with quote
            "''Beefeater's cool''",
            // Cursive with bold
            "''A '''Game''' of Thrones''",
            // Bold with cursive
            "'''A ''little'' bit'''",
            // Cursive with one character
            "''B''",
            // Cursive with two characters
            "''Be''",
        }
    )
    void testFindCursive(String cursive) {
        String text = String.format("A %s.", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        assertEquals(1, matches.size());
        Assertions.assertEquals(cursive, matches.get(0).getText());
    }

    @Test
    void testCursiveSimpleFinishing() {
        String cursive = "''cursive''";
        String text = String.format("%s", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        assertEquals(1, matches.size());
        Assertions.assertEquals(cursive, matches.get(0).getText());
    }

    @Test
    void testCursiveDouble() {
        String cursive1 = "''cursive1''";
        String cursive2 = "''cursive2''";
        String text = String.format("A %s and %s.", cursive1, cursive2);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        assertEquals(Set.of(cursive1, cursive2), matches.stream().map(Immutable::getText).collect(Collectors.toSet()));
    }

    @Test
    void testCursiveTruncated() {
        String cursive1 = "''xxx''";
        String cursive2 = "''yyy"; // Truncated with new line
        String cursive3 = "''zzz''";
        String cursive4 = "''aaa"; // Truncated with end
        String text = String.format("%s %s\n %s %s.", cursive1, cursive2, cursive3, cursive4);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        assertEquals(
            Set.of(cursive1, cursive2, cursive3),
            matches.stream().map(Immutable::getText).collect(Collectors.toSet())
        );
    }

    @Test
    void testBoldDouble() {
        String bold1 = "'''bold1'''";
        String bold2 = "'''bold2'''";
        String text = String.format("A %s and %s.", bold1, bold2);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        assertEquals(Set.of(bold1, bold2), matches.stream().map(Immutable::getText).collect(Collectors.toSet()));
    }

    @Test
    void testBoldTruncated() {
        String bold1 = "'''xxx'''";
        String bold2 = "'''yyy"; // Truncated with new line
        String bold3 = "'''zzz'''";
        String bold4 = "'''aaa"; // Truncated with end
        String text = String.format("%s %s\n %s %s.", bold1, bold2, bold3, bold4);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        assertEquals(Set.of(bold1, bold2, bold3), matches.stream().map(Immutable::getText).collect(Collectors.toSet()));
    }

    @Test
    void testCursiveWithNestedQuotes() {
        String cursive =
            "La leyenda: ''Y \"será más terrorífica que 'La Mujer de Judas'\"''. La producción será ''[[Almas en Pena]]''.";
        String text = String.format("A %s.", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        assertEquals(2, matches.size());
    }

    @Test
    void testBlankCursive() {
        String cursive = "A blank '' '' cursive.";
        String text = String.format("%s", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        assertTrue(matches.isEmpty());
    }

    @Test
    void testBlankCursiveWithNewLine() {
        String cursive = "A blank cursive ''\n";
        String text = String.format("%s", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        assertTrue(matches.isEmpty());
    }
}
