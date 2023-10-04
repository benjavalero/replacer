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
@SpringBootTest(classes = SpaceIncorrectFinder.class)
class SpaceIncorrectFinderTest {

    @Autowired
    private SpaceIncorrectFinder spaceIncorrectFinder;

    @ParameterizedTest
    @CsvSource(
        value = {
            "[[File:x.jpeg|test]], [[Archivo:x.jpeg|test]]",
            "[[FIle:x.jpeg|test]], [[Archivo:x.jpeg|test]]",
            "[[image:x.png]], [[Imagen:x.png]]",
            "[[Annex:Países]], [[Anexo:Países]]",
            "[[Category:Animal]], [[Categoría:Animal]]",
        }
    )
    void testNotTranslatedSpace(String text, String fix) {
        List<Cosmetic> cosmetics = spaceIncorrectFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(
        strings = { "[[Archivo:x.jpeg|test]]", "[[Imagen:x.png]]", "[[Anexo:Países]]", "[[Categoría:Animal]]" }
    )
    void testValidSpace(String text) {
        List<Cosmetic> cosmetics = spaceIncorrectFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }

    @ParameterizedTest
    @CsvSource(value = { "[[File:x.jpeg|test]], [[Ficheiro:x.jpeg|test]]", "[[image:x.png]], [[Imaxe:x.png]]" })
    void testNotTranslatedSpaceInGalician(String text, String fix) {
        FinderPage page = FinderPage.of(WikipediaLanguage.GALICIAN, "", text);
        List<Cosmetic> cosmetics = IterableUtils.toList(spaceIncorrectFinder.find(page));

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(strings = { "[[Arquivo:z.jpg]]" })
    void testValidSpaceInGalician(String text) {
        FinderPage page = FinderPage.of(WikipediaLanguage.GALICIAN, "", text);
        List<Cosmetic> cosmetics = IterableUtils.toList(spaceIncorrectFinder.find(page));

        assertTrue(cosmetics.isEmpty());
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "[[archivo:x.jpeg|test]], [[Archivo:x.jpeg|test]]",
            "[[imagen:x.png]], [[Imagen:x.png]]",
            "[[anexo:Discografía de Queen]], [[Anexo:Discografía de Queen]]",
            "[[categoría:Animal]], [[Categoría:Animal]]",
        }
    )
    void testLowercaseSpace(String text, String fix) {
        List<Cosmetic> cosmetics = spaceIncorrectFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @Test
    void testLowercaseSpaceInGalician() {
        String text = "[[arquivo:xxx.jpg]]";
        String fix = "[[Ficheiro:xxx.jpg]]";

        List<Cosmetic> cosmetics = IterableUtils.toList(
            spaceIncorrectFinder.find(FinderPage.of(WikipediaLanguage.GALICIAN, "X", text))
        );

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }
}
