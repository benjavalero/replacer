package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class CategoryFinderTest {

    private String text;
    private Set<MatchResult> expected;

    @Before
    public void setUp() {
        String category1 = "[[Categoría:Países]]";
        String category2 = "[[Categoría:Obras españolas]]";
        this.text = String.format("%s %s", category1, category2);

        this.expected = new HashSet<>();
        this.expected.add(new MatchResult(0, category1));
        this.expected.add(new MatchResult(21, category2));
    }

    @Test
    public void testCategoryRegexFinder() {
        CategoryRegexFinder finder = new CategoryRegexFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCategoryRegexClassFinder() {
        CategoryRegexClassFinder finder = new CategoryRegexClassFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCategoryRegexClassLazyFinder() {
        CategoryRegexClassLazyFinder finder = new CategoryRegexClassLazyFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCategoryRegexClassPossessiveFinder() {
        CategoryRegexClassPossessiveFinder finder = new CategoryRegexClassPossessiveFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testCategoryAutomatonFinder() {
        CategoryAutomatonFinder finder = new CategoryAutomatonFinder();
        Assert.assertEquals(expected, finder.findMatches(text));
    }

}
