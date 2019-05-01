package es.bvalero.replacer.misspelling;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UppercaseFinderTest {

    private Collection<String> words;
    private String text;
    private Set<WordMatch> expected;

    @Before
    public void setUp() {
        this.words = Arrays.asList("Enero", "Febrero", "Lunes", "Martes");
        this.text = "=Enero. Febrero, Lunes #  Martes.";

        this.expected = new HashSet<>();
        this.expected.add(new WordMatch(1, "Enero"));
        this.expected.add(new WordMatch(8, "Febrero"));
        this.expected.add(new WordMatch(26, "Martes"));
    }

    @Test
    public void testFindUppercaseIndexOf() {
        UppercaseIndexOfFinder finder = new UppercaseIndexOfFinder(words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindUppercaseMatch() {
        UppercaseMatchFinder finder = new UppercaseMatchFinder(words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindUppercaseAutomaton() {
        UppercaseAutomatonFinder finder = new UppercaseAutomatonFinder(words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindUppercaseMatchLookBehind() {
        UppercaseMatchLookBehindFinder finder = new UppercaseMatchLookBehindFinder(words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindUppercaseRegexAlternate() {
        UppercaseRegexAlternateFinder finder = new UppercaseRegexAlternateFinder(words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindUppercaseAutomatonAlternate() {
        UppercaseAutomatonAlternateFinder finder = new UppercaseAutomatonAlternateFinder(words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindUppercaseRegexAlternateLookBehind() {
        UppercaseRegexAlternateLookBehindFinder finder = new UppercaseRegexAlternateLookBehindFinder(words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

}
