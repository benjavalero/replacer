package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CategoryFinderTest {

    @Test
    public void testRegexComment() {
        String category1 = "[[Categoría:Países]]";
        String category2 = "[[Categoría:Obras españolas|Españolas, Obras]]";
        String text = String.format("%s %s", category1, category2);

        ImmutableFinder categoryFinder = new CategoryFinder();

        List<Immutable> matches = categoryFinder.findList(text);
        Assertions.assertEquals(2, matches.size());
        Assertions.assertEquals(category1, matches.get(0).getText());
        Assertions.assertEquals(category2, matches.get(1).getText());
    }
}
