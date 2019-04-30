package es.bvalero.replacer.misspelling;

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
    private Set<WordMatch> expected;

    @Before
    public void setUp() {
        this.words = Arrays.asList("Domingo", "Frances", "Julio", "Sidney");
        this.text = "Con Julio Verne, Frances McDormand y Francesco en Julio de 2019.";

        this.expected = new HashSet<>();
        this.expected.add(new WordMatch(4, "Julio"));
        this.expected.add(new WordMatch(17, "Frances"));
    }

    @Test
    public void testFindPersonIndexOf() {
        PersonIndexOfFinder finder = new PersonIndexOfFinder(words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindPersonMatch() {
        PersonMatchFinder finder = new PersonMatchFinder(words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindPersonAutomaton() {
        PersonAutomatonFinder finder = new PersonAutomatonFinder(words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindPersonMatchComplete() {
        PersonMatchCompleteFinder finder = new PersonMatchCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindPersonAutomatonComplete() {
        PersonAutomatonCompleteFinder finder = new PersonAutomatonCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindPersonRegexAlternate() {
        PersonRegexAlternateFinder finder = new PersonRegexAlternateFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindPersonAutomatonAlternate() {
        PersonAutomatonAlternateFinder finder = new PersonAutomatonAlternateFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindPersonRegexAlternateComplete() {
        PersonRegexAlternateCompleteFinder finder = new PersonRegexAlternateCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindPersonAutomatonAlternateComplete() {
        PersonAutomatonAlternateCompleteFinder finder = new PersonAutomatonAlternateCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindPersonMatchAll() {
        PersonMatchAllFinder finder = new PersonMatchAllFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindPersonAutomatonAll() {
        PersonAutomatonAllFinder finder = new PersonAutomatonAllFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

    @Test
    public void testFindPersonMatchAllComplete() {
        PersonMatchAllCompleteFinder finder = new PersonMatchAllCompleteFinder(this.words);
        Assert.assertEquals(expected, finder.findWords(text));
    }

}
