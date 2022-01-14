package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.immutable.Immutable;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { PersonSurnameFinder.class, XmlConfiguration.class })
class PersonSurnameFinderTest {

    @Autowired
    private PersonSurnameFinder personSurnameFinder;

    @ParameterizedTest
    @CsvSource(
        value = {
            "A Plácido Domingo, Domingo",
            "A Álvaro Pinto, Pinto",
            "En RCA Records, Records",
            "Juegos Olímpicos de Verano, de Verano",
        }
    )
    void testFindPersonSurnames(String text, String noun) {
        List<Immutable> matches = personSurnameFinder.findList(text);

        assertEquals(1, matches.size());
        assertEquals(noun, matches.get(0).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "A varios Records", "Juegos Olímpicos de verano", "A Juan Pintor" })
    void testFindPersonSurnamesNonValid(String text) {
        List<Immutable> matches = personSurnameFinder.findList(text);

        assertTrue(matches.isEmpty());
    }

    @ParameterizedTest
    @CsvSource(value = { "Revolución de Octubre, de Octubre", "Estadio 12 de Octubre, 12 de Octubre" })
    void testFindMonthsAsSurnames(String text, String noun) {
        List<Immutable> matches = personSurnameFinder.findList(text);

        assertEquals(1, matches.size());
        assertEquals(noun, matches.get(0).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "Uzbekistan Airlines" })
    void testFindPersonSurnamesWithNames(String text) {
        List<Immutable> matches = personSurnameFinder.findList(text);

        assertEquals(1, matches.size());
        assertEquals(text, matches.get(0).getText());
    }
}
