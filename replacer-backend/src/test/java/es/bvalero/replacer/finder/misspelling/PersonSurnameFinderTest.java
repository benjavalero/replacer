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

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(noun, matches.get(0).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "A varios Records", "Juegos Olímpicos de verano" })
    void testFindPersonSurnamesNonValid(String text) {
        List<Immutable> matches = personSurnameFinder.findList(text);

        Assertions.assertTrue(matches.isEmpty());
    }
}
