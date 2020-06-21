package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.finder.Immutable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { PersonNameFinder.class, XmlConfiguration.class })
class PersonNameFinderTest {
    @Autowired
    private PersonNameFinder personNameFinder;

    @Test
    void testRegexPersonName() {
        String noun = "Julio";
        String surname = "Verne";
        String text = String.format("A %s %s %ss %s %s %s.", noun, surname, noun, noun, surname.toLowerCase(), noun);

        List<Immutable> matches = personNameFinder.findList(text);

        Set<String> expected = Collections.singleton(noun);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }
}
