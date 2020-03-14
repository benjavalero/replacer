package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CategoryFinderTest {

    @Test
    public void testRegexComment() {
        String category1 = "[[Categoría:Países]]";
        String category2 = "[[Categoría:Obras españolas]]";
        String text = String.format("%s %s", category1, category2);

        ImmutableFinder categoryFinder = new CategoryFinder();

        List<Immutable> matches = categoryFinder.findList(text);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(category1, matches.get(0).getText());
        Assert.assertEquals(category2, matches.get(1).getText());
    }
}
