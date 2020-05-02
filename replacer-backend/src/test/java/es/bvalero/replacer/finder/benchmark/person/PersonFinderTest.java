package es.bvalero.replacer.finder.benchmark.person;

import java.util.*;

import es.bvalero.replacer.finder.benchmark.FinderResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class PersonFinderTest {
    private Collection<String> words;
    private String text;
    private Set<FinderResult> expected;

    @BeforeEach
    public void setUp() {
        this.words = Arrays.asList("Domingo", "Frances", "Julio", "Sidney");

        String noun = "Julio";
        String surname = "Verne";

        this.text = String.format("A %s %s %ss %s %s %s.", noun, surname, noun, noun, surname.toLowerCase(), noun);

        this.expected = new HashSet<>();
        this.expected.add(FinderResult.of(2, "Julio"));
    }

    @Test
    public void testPersonIndexOfFinder() {
        PersonIndexOfFinder finder = new PersonIndexOfFinder(words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonRegexFinder() {
        PersonRegexFinder finder = new PersonRegexFinder(words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonAutomatonFinder() {
        PersonAutomatonFinder finder = new PersonAutomatonFinder(words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonRegexCompleteFinder() {
        PersonRegexCompleteFinder finder = new PersonRegexCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonAutomatonCompleteFinder() {
        PersonAutomatonCompleteFinder finder = new PersonAutomatonCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonRegexAlternateFinder() {
        PersonRegexAlternateFinder finder = new PersonRegexAlternateFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonAutomatonAlternateFinder() {
        PersonAutomatonAlternateFinder finder = new PersonAutomatonAlternateFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonRegexAlternateCompleteFinder() {
        PersonRegexAlternateCompleteFinder finder = new PersonRegexAlternateCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonAutomatonAlternateCompleteFinder() {
        PersonAutomatonAlternateCompleteFinder finder = new PersonAutomatonAlternateCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonRegexAllFinder() {
        PersonRegexAllFinder finder = new PersonRegexAllFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonAutomatonAllFinder() {
        PersonAutomatonAllFinder finder = new PersonAutomatonAllFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testPersonRegexAllCompleteFinder() {
        PersonRegexAllCompleteFinder finder = new PersonRegexAllCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }
}
