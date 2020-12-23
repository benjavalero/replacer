package es.bvalero.replacer.finder.benchmark.surname;

import es.bvalero.replacer.finder.benchmark.FinderResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class SurnameFinderTest {

    private Collection<String> words;
    private String text;
    private Set<FinderResult> expected;

    @BeforeEach
    public void setUp() {
        this.words = Arrays.asList("Online", "Records", "de Verano", "Pinto");

        this.text = "En News Online, Álvaro Pinto, Victor Records, Juegos Olímpicos de Verano.";

        this.expected = new HashSet<>();
        this.expected.add(FinderResult.of(8, "Online"));
        this.expected.add(FinderResult.of(23, "Pinto"));
        this.expected.add(FinderResult.of(37, "Records"));
        this.expected.add(FinderResult.of(63, "de Verano"));
    }

    @Test
    void testSurnameIndexOfFinder() {
        SurnameIndexOfFinder finder = new SurnameIndexOfFinder(words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameRegexFinder() {
        SurnameRegexFinder finder = new SurnameRegexFinder(words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameAutomatonFinder() {
        SurnameAutomatonFinder finder = new SurnameAutomatonFinder(words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameRegexCompleteFinder() {
        SurnameRegexCompleteFinder finder = new SurnameRegexCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameAutomatonCompleteFinder() {
        SurnameAutomatonCompleteFinder finder = new SurnameAutomatonCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameRegexAlternateFinder() {
        SurnameRegexAlternateFinder finder = new SurnameRegexAlternateFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameAutomatonAlternateFinder() {
        SurnameAutomatonAlternateFinder finder = new SurnameAutomatonAlternateFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameRegexAlternateCompleteFinder() {
        SurnameRegexAlternateCompleteFinder finder = new SurnameRegexAlternateCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testSurnameAutomatonAlternateCompleteFinder() {
        SurnameAutomatonAlternateCompleteFinder finder = new SurnameAutomatonAlternateCompleteFinder(this.words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }
}
