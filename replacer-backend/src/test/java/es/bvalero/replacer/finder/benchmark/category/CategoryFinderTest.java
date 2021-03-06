package es.bvalero.replacer.finder.benchmark.category;

import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CategoryFinderTest {

    private String text;
    private Set<BenchmarkResult> expected;

    @BeforeEach
    public void setUp() {
        String category1 = "[[Categoría:Países]]";
        String category2 = "[[Categoría:Obras españolas|Españolas, Obras]]";
        this.text = String.format("%s %s", category1, category2);

        this.expected = new HashSet<>();
        this.expected.add(BenchmarkResult.of(0, category1));
        this.expected.add(BenchmarkResult.of(21, category2));
    }

    @Test
    void testCategoryRegexFinder() {
        CategoryRegexFinder finder = new CategoryRegexFinder();
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testCategoryAutomatonFinder() {
        CategoryAutomatonFinder finder = new CategoryAutomatonFinder();
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testCategoryLinearFinder() {
        CategoryLinearFinder finder = new CategoryLinearFinder();
        Assertions.assertEquals(expected, finder.findMatches(text));
    }
}
