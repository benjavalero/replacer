package es.bvalero.replacer.finder.benchmark.person;

import java.util.*;

import es.bvalero.replacer.finder.benchmark.FinderResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

class PersonFinderTest {
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
    void testPersonIndexOfFinder() {
        PersonIndexOfFinder finder = new PersonIndexOfFinder(words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonRegexFinder() {
        PersonRegexFinder finder = new PersonRegexFinder(words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonAutomatonFinder() {
        PersonAutomatonFinder finder = new PersonAutomatonFinder(words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonRegexCompleteFinder() {
        PersonRegexCompleteFinder finder = new PersonRegexCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonAutomatonCompleteFinder() {
        PersonAutomatonCompleteFinder finder = new PersonAutomatonCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonRegexAlternateFinder() {
        PersonRegexAlternateFinder finder = new PersonRegexAlternateFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonAutomatonAlternateFinder() {
        PersonAutomatonAlternateFinder finder = new PersonAutomatonAlternateFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonRegexAlternateCompleteFinder() {
        PersonRegexAlternateCompleteFinder finder = new PersonRegexAlternateCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonAutomatonAlternateCompleteFinder() {
        PersonAutomatonAlternateCompleteFinder finder = new PersonAutomatonAlternateCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonRegexAllFinder() {
        PersonRegexAllFinder finder = new PersonRegexAllFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonAutomatonAllFinder() {
        PersonAutomatonAllFinder finder = new PersonAutomatonAllFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testPersonRegexAllCompleteFinder() {
        PersonRegexAllCompleteFinder finder = new PersonRegexAllCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }
}
