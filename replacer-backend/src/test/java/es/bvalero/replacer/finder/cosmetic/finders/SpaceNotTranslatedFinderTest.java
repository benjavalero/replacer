package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.Cosmetic;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaOfflineService;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaService;
import java.util.List;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("offline")
@SpringBootTest(
    classes = { SpaceNotTranslatedFinder.class, XmlConfiguration.class, CheckWikipediaOfflineService.class }
)
class SpaceNotTranslatedFinderTest {

    @Autowired
    private CheckWikipediaService checkWikipediaService;

    @Autowired
    private SpaceNotTranslatedFinder spaceNotTranslatedFinder;

    @ParameterizedTest
    @CsvSource(
        value = {
            "[[File:x.jpeg|test]], [[Archivo:x.jpeg|test]]",
            "[[image:x.png]], [[Imagen:x.png]]",
            "[[Category:Animal]], [[Categoría:Animal]]",
        }
    )
    void testNotTranslatedSpaceFinder(String text, String fix) {
        List<Cosmetic> cosmetics = spaceNotTranslatedFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @CsvSource(value = { "[[File:x.jpeg|test]], [[Ficheiro:x.jpeg|test]]", "[[image:x.png]], [[Imaxe:x.png]]" })
    void testNotTranslatedSpaceInGalician(String text, String fix) {
        FinderPage page = FinderPage.of(WikipediaLanguage.GALICIAN, text, "");
        List<Cosmetic> cosmetics = IterableUtils.toList(spaceNotTranslatedFinder.find(page));

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }
}
