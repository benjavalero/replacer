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
@SpringBootTest(classes = { CategoryEnglishFinder.class, XmlConfiguration.class, CheckWikipediaOfflineService.class })
class CategoryEnglishFinderTest {

    @Autowired
    private CheckWikipediaService checkWikipediaService;

    @Autowired
    private CategoryEnglishFinder categoryEnglishFinder;

    @ParameterizedTest
    @CsvSource(value = { "[[Category:Animal]], [[Categoría:Animal]]", "[[category:Animal]], [[Categoría:Animal]]" })
    void testCategoryInEnglish(String text, String fix) {
        List<Cosmetic> cosmetics = categoryEnglishFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(strings = { "[[Categoría:Animal]]" })
    void testCategoryInSpanish(String text) {
        List<Cosmetic> cosmetics = categoryEnglishFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
