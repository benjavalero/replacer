package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CursiveFinderTest {

    @Test
    public void testCursiveSimple() {
        String cursive = "''cursive''";
        String text = String.format("A %s.", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(cursive, matches.get(0).getText());
    }

    @Test
    public void testCursiveSimpleFinishing() {
        String cursive = "''cursive''";
        String text = String.format("%s", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(cursive, matches.get(0).getText());
    }

    @Test
    public void testCursiveDouble() {
        String cursive1 = "''cursive1''";
        String cursive2 = "''cursive2''";
        String text = String.format("A %s and %s.", cursive1, cursive2);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assertions.assertEquals(2, matches.size());
        Assertions.assertEquals(cursive1, matches.get(0).getText());
        Assertions.assertEquals(cursive2, matches.get(1).getText());
    }

    @Test
    public void testCursiveTruncated() {
        String cursive1 = "''cursive1";
        String cursive2 = "''cursive2''";
        String text = String.format("A %s\n and %s.", cursive1, cursive2);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(cursive1, cursive2));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testBoldSimple() {
        String bold = "'''bold'''";
        String text = String.format("A %s.", bold);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(bold, matches.get(0).getText());
    }

    @Test
    public void testBoldDouble() {
        String bold1 = "'''bold1'''";
        String bold2 = "'''bold2'''";
        String text = String.format("A %s and %s.", bold1, bold2);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assertions.assertEquals(2, matches.size());
        Assertions.assertEquals(bold1, matches.get(0).getText());
        Assertions.assertEquals(bold2, matches.get(1).getText());
    }

    @Test
    public void testBoldTruncated() {
        String bold1 = "'''bold";
        String bold2 = "'''bold'''";
        String text = String.format("A %s\n and %s.", bold1, bold2);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(bold1, bold2));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testCursiveWithQuote() {
        // We need to capture more than one character after the inner quote
        String cursive = "''Beefeater's cool''";
        String text = String.format("A %s.", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(cursive, matches.get(0).getText());
    }

    @Test
    public void testCursiveWithNestedQuotes() {
        String cursive =
            "La leyenda: ''Y \"será más terrorífica que 'La Mujer de Judas'\"''. La producción será ''[[Almas en Pena]]''.";
        String text = String.format("A %s.", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assertions.assertEquals(2, matches.size());
    }

    @Test
    public void testCursiveWithBold() {
        String cursive = "''A '''Game''' of Thrones''";
        String text = String.format("A %s.", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(cursive, matches.get(0).getText());
    }

    @Test
    public void testCursiveWithOneCharacter() {
        // We need to capture more than one character between the quotes
        String cursive = "''B''";
        String text = String.format("A %s.", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(cursive, matches.get(0).getText());
    }

    @Test
    public void testCursiveWithTwoCharacters() {
        String cursive = "''Be''";
        String text = String.format("A %s.", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(cursive, matches.get(0).getText());
    }
}
