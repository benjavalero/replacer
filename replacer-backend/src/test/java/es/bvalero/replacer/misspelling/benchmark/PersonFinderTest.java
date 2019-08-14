package es.bvalero.replacer.misspelling.benchmark;

import es.bvalero.replacer.finder.MatchResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PersonFinderTest {

    private Collection<String> words;
    private String text;
    private Set<MatchResult> expected;

    @Before
    public void setUp() {
        this.words = Arrays.asList("Domingo", "Frances", "Julio", "Sidney");
        this.text = "Con Julio Verne, Frances McDormand y Francesco en Julio de 2019.";

        this.expected = new HashSet<>();
        this.expected.add(MatchResult.of(4, "Julio"));
        this.expected.add(MatchResult.of(17, "Frances"));
    }

    @Test
    public void testPersonIndexOfFinder() {
        PersonIndexOfFinder finder = new PersonIndexOfFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonRegexFinder() {
        PersonRegexFinder finder = new PersonRegexFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonAutomatonFinder() {
        PersonAutomatonFinder finder = new PersonAutomatonFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonRegexCompleteFinder() {
        PersonRegexCompleteFinder finder = new PersonRegexCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonAutomatonCompleteFinder() {
        PersonAutomatonCompleteFinder finder = new PersonAutomatonCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonAlternateRegexFinder() {
        PersonAlternateRegexFinder finder = new PersonAlternateRegexFinder(this.words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonAlternateAutomatonFinder() {
        PersonAlternateAutomatonFinder finder = new PersonAlternateAutomatonFinder(this.words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonAlternateRegexCompleteFinder() {
        PersonAlternateRegexCompleteFinder finder = new PersonAlternateRegexCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonAlternateAutomatonCompleteFinder() {
        PersonAlternateAutomatonCompleteFinder finder = new PersonAlternateAutomatonCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonRegexAllFinder() {
        PersonRegexAllFinder finder = new PersonRegexAllFinder(this.words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonAutomatonAllFinder() {
        PersonAutomatonAllFinder finder = new PersonAutomatonAllFinder(this.words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonRegexAllCompleteFinder() {
        PersonRegexAllCompleteFinder finder = new PersonRegexAllCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

}
