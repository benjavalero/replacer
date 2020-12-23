package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CategoryFinderTest {

    private final CategoryFinder categoryFinder = new CategoryFinder();

    @ParameterizedTest
    @ValueSource(
        strings = {
            "[[Categoría:Países]]", "[[Categoría:Obras españolas|Españolas, Obras]]", "[[Categoría:Informática| ]]",
        }
    )
    void testFindCategories(String text) {
        List<Immutable> matches = categoryFinder.findList(text);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(text, matches.get(0).getText());
    }
}
