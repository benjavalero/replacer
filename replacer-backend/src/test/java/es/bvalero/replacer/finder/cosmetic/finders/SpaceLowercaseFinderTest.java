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
@SpringBootTest(classes = { SpaceLowercaseFinder.class, XmlConfiguration.class, CheckWikipediaOfflineService.class })
class SpaceLowercaseFinderTest {

    @Autowired
    private CheckWikipediaService checkWikipediaService;

    @Autowired
    private SpaceLowercaseFinder spaceLowercaseFinder;

    @ParameterizedTest
    @CsvSource(
        value = {
            "[[archivo:x.jpeg|test]], [[Archivo:x.jpeg|test]]",
            "[[imagen:x.png]], [[Imagen:x.png]]",
            "[[anexo:Discografía de Queen]], [[Anexo:Discografía de Queen]]",
            "[[categoría:Animal]], [[Categoría:Animal]]",
        }
    )
    void testLowercaseSpaceFinder(String text, String fix) {
        List<Cosmetic> cosmetics = spaceLowercaseFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(strings = { "[[Archivo:x.pdf]]" })
    void testValidSpace(String text) {
        List<Cosmetic> cosmetics = spaceLowercaseFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
