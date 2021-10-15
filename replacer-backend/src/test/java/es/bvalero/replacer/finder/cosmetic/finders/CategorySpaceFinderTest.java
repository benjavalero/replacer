package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.cosmetic.Cosmetic;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaOfflineService;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaService;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("offline")
@SpringBootTest(classes = { CategorySpaceFinder.class, XmlConfiguration.class, CheckWikipediaOfflineService.class })
class CategorySpaceFinderTest {

    @Autowired
    private CheckWikipediaService checkWikipediaService;

    @Autowired
    private CategorySpaceFinder categorySpaceFinder;

    @ParameterizedTest
    @CsvSource(
        value = {
            "[[ Categoría:Animal]], [[Categoría:Animal]]",
            "[[Categoría :Animal]], [[Categoría:Animal]]",
            "[[Categoría: Animal]], [[Categoría:Animal]]",
            "[[Categoría:Animal ]], [[Categoría:Animal]]",
        }
    )
    void testCategoryWithSpace(String text, String fix) {
        List<Cosmetic> cosmetics = categorySpaceFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(strings = { "[[Categoría:Animal]]" })
    void testCategoryWithNoSpace(String text) {
        List<Cosmetic> cosmetics = categorySpaceFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
