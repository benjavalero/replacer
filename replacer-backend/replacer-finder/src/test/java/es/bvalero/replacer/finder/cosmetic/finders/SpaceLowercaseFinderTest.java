package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.Cosmetic;
import es.bvalero.replacer.finder.FinderPage;
import java.util.List;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("offline")
@EnableConfigurationProperties(FinderProperties.class)
@SpringBootTest(classes = SpaceLowercaseFinder.class)
class SpaceLowercaseFinderTest {

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
    @ValueSource(
        strings = { "[[Archivo:x.pdf]]", "[[Imagen:x.png]]", "[[Anexo:Discografía de Queen]]", "[[Categoría:Animal]]" }
    )
    void testValidSpace(String text) {
        List<Cosmetic> cosmetics = spaceLowercaseFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }

    @Test
    void testGalicianFile() {
        String text = "[[arquivo:xxx.jpg]]";
        String fix = "[[Arquivo:xxx.jpg]]";

        List<Cosmetic> cosmetics = IterableUtils.toList(
            spaceLowercaseFinder.find(FinderPage.of(WikipediaLanguage.GALICIAN, "X", text))
        );

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }
}
