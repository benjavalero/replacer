package es.bvalero.replacer.finder.benchmark.person;

import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PersonFinderTest {

    private Collection<String> words;
    private String text;
    private Set<FinderResult> expected;

    @BeforeEach
    public void setUp() {
        this.words = Arrays.asList("Sky", "Julio", "Los Angeles", "Tokyo");

        this.text = "En Sky News, Julio Álvarez, Los Angeles Lakers, Tokyo TV, José Julio García.";

        this.expected = new HashSet<>();
        this.expected.add(FinderResult.of(3, "Sky"));
        this.expected.add(FinderResult.of(13, "Julio"));
        this.expected.add(FinderResult.of(28, "Los Angeles"));
        this.expected.add(FinderResult.of(48, "Tokyo"));
        this.expected.add(FinderResult.of(63, "Julio"));
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
}
