package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.Cosmetic;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("offline")
@EnableConfigurationProperties(FinderProperties.class)
@SpringBootTest(classes = CategoryWhiteSpaceFinder.class)
class CategoryWhiteSpaceFinderTest {

    @Autowired
    private CategoryWhiteSpaceFinder categoryWhiteSpaceFinder;

    @ParameterizedTest
    @CsvSource(
        value = {
            "[[ Categoría:Animal]], [[Categoría:Animal]]",
            "[[Categoría :Animal]], [[Categoría:Animal]]",
            "[[Categoría: Animal]], [[Categoría:Animal]]",
            "[[Categoría:Animal ]], [[Categoría:Animal]]",
            "[[Categoría:Animal| Animal ]], [[Categoría:Animal|Animal]]",
        }
    )
    void testCategoryWithSpace(String text, String fix) {
        List<Cosmetic> cosmetics = categoryWhiteSpaceFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(strings = { "[[Categoría:Animal]]", "[[Categoría:Animal| ]]" })
    void testCategoryWithNoSpace(String text) {
        List<Cosmetic> cosmetics = categoryWhiteSpaceFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
