package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CategoryFinderTest {

    @Test
    public void testRegexComment() {
        String category1 = "[[Categoría:Países]]";
        String category2 = "[[Categoría:Obras españolas]]";
        String text = String.format("%s %s", category1, category2);

        IgnoredReplacementFinder categoryFinder = new CategoryFinder();

        List<MatchResult> matches = categoryFinder.findIgnoredReplacements(text);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(category1, matches.get(0).getText());
        Assert.assertEquals(category2, matches.get(1).getText());
    }

}
