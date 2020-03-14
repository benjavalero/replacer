package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CursiveFinderTest {

    @Test
    public void testCursiveSimple() {
        String cursive = "''cursive''";
        String text = String.format("A %s.", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assert.assertEquals(1, matches.size());
        Assert.assertEquals(cursive, matches.get(0).getText());
    }

    @Test
    public void testCursiveDouble() {
        String cursive1 = "''cursive1''";
        String cursive2 = "''cursive2''";
        String text = String.format("A %s and %s.", cursive1, cursive2);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(cursive1, matches.get(0).getText());
        Assert.assertEquals(cursive2, matches.get(1).getText());
    }

    @Test
    public void testCursiveTruncated() {
        String cursive = "''cursive\n";
        String text = String.format("A %sA", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assert.assertEquals(1, matches.size());
        Assert.assertEquals(cursive, matches.get(0).getText());
    }

    @Test
    public void testBoldSimple() {
        String bold = "'''bold'''";
        String text = String.format("A %s.", bold);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assert.assertEquals(0, matches.size());
    }

    @Test
    public void testBoldDouble() {
        String bold1 = "'''bold1'''";
        String bold2 = "'''bold2'''";
        String text = String.format("A %s and %s.", bold1, bold2);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assert.assertEquals(0, matches.size());
    }

    @Test
    public void testBoldTruncated() {
        String bold = "'''bold\n";
        String text = String.format("A %sA", bold);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assert.assertEquals(0, matches.size());
    }

    @Test
    public void testCursiveWithQuote() {
        // We need to capture more than one character after the inner quote
        String cursive = "''Beefeater's cool''";
        String text = String.format("A %s.", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assert.assertEquals(1, matches.size());
        Assert.assertEquals(cursive, matches.get(0).getText());
    }

    @Test
    public void testCursiveWithBold() {
        String cursive = "''A '''Game''' of Thrones''";
        String text = String.format("A %s.", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assert.assertEquals(1, matches.size());
        Assert.assertEquals(cursive, matches.get(0).getText());
    }

    @Test
    public void testCursiveWithOneCharacter() {
        // We need to capture more than one character between the quotes
        String cursive = "''B''";
        String text = String.format("A %s.", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assert.assertEquals(0, matches.size());
    }

    @Test
    public void testCursiveWithTwoCharacters() {
        String cursive = "''Be''";
        String text = String.format("A %s.", cursive);

        ImmutableFinder cursiveFinder = new CursiveFinder();
        List<Immutable> matches = cursiveFinder.findList(text);

        Assert.assertEquals(1, matches.size());
        Assert.assertEquals(cursive, matches.get(0).getText());
    }
}
