package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.finder.Immutable;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { PersonNameFinder.class, XmlConfiguration.class })
class PersonNameFinderTest {

    @Autowired
    private PersonNameFinder personNameFinder;

    @ParameterizedTest
    @CsvSource(
        value = {
            "Sky News, Sky",
            "Julio Álvarez, Julio",
            "Los Angeles Lakers, Los Angeles",
            "Tokyo TV, Tokyo",
            "José Julio Domingo, Julio",
        }
    )
    void testFindPersonNames(String text, String noun) {
        List<Immutable> matches = personNameFinder.findList(text);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(noun, matches.get(0).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "En julio de 2000", "En Julio de 2000" })
    void testFindPersonNamesNonValid(String text) {
        List<Immutable> matches = personNameFinder.findList(text);

        Assertions.assertTrue(matches.isEmpty());
    }
}
