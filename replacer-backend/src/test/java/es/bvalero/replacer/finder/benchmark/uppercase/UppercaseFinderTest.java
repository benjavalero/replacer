package es.bvalero.replacer.finder.benchmark.uppercase;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import es.bvalero.replacer.finder.benchmark.FinderResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UppercaseFinderTest {
    private Collection<String> words;
    private String text;
    private Set<FinderResult> expected;

    @BeforeEach
    public void setUp() {
        this.words = Arrays.asList("Enero", "Febrero", "Lunes", "Martes");
        this.text = "=Enero. Febrero, Lunes #  Martes.";

        this.expected = new HashSet<>();
        this.expected.add(FinderResult.of(1, "Enero"));
        this.expected.add(FinderResult.of(8, "Febrero"));
        this.expected.add(FinderResult.of(26, "Martes"));
    }

    @Test
    public void testUppercaseIndexOfFinder() {
        UppercaseIndexOfFinder finder = new UppercaseIndexOfFinder(words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testUppercaseRegexIterateFinder() {
        UppercaseRegexIterateFinder finder = new UppercaseRegexIterateFinder(words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testUppercaseAutomatonIterateFinder() {
        UppercaseAutomatonIterateFinder finder = new UppercaseAutomatonIterateFinder(words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testUppercaseRegexLookBehindFinder() {
        UppercaseRegexLookBehindFinder finder = new UppercaseRegexLookBehindFinder(words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testUppercaseRegexAlternateFinder() {
        UppercaseRegexAlternateFinder finder = new UppercaseRegexAlternateFinder(words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testUppercaseAutomatonAlternateFinder() {
        UppercaseAutomatonAlternateFinder finder = new UppercaseAutomatonAlternateFinder(words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testUppercaseRegexAlternateLookBehindFinder() {
        UppercaseRegexAlternateLookBehindFinder finder = new UppercaseRegexAlternateLookBehindFinder(words);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }
}
