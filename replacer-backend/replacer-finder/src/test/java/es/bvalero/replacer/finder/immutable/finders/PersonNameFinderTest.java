package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.Immutable;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@EnableConfigurationProperties(FinderProperties.class)
@SpringBootTest(classes = PersonNameFinder.class)
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
    void testPersonNames(String text, String noun) {
        List<Immutable> matches = personNameFinder.findList(text);

        assertEquals(1, matches.size());
        assertEquals(noun, matches.get(0).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "En julio de 2000", "En Julio de 2000" })
    void testPersonNamesNonValid(String text) {
        List<Immutable> matches = personNameFinder.findList(text);

        assertTrue(matches.isEmpty());
    }
}
