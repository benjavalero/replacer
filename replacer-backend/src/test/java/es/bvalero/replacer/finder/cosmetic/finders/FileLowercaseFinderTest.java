package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.cosmetic.Cosmetic;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { FileLowercaseFinder.class, XmlConfiguration.class })
class FileLowercaseFinderTest {

    @Autowired
    private FileLowercaseFinder fileLowercaseFinder;

    @ParameterizedTest
    @CsvSource(value = { "[[archivo:x.jpeg|test]], [[Archivo:x.jpeg|test]]", "[[image:x.png]], [[Image:x.png]]" })
    void testFileLowercaseFinder(String text, String fix) {
        List<Cosmetic> cosmetics = fileLowercaseFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(strings = { "[[File:x.pdf]]" })
    void testSameLinkNotValid(String text) {
        List<Cosmetic> cosmetics = fileLowercaseFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
