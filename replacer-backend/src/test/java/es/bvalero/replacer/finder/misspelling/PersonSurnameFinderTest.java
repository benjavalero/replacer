package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.finder.Immutable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PersonSurnameFinderTest {

    @Test
    public void testRegexPersonSurname() {
        String noun = "RCA";
        String surname = "Records";
        String text = String.format("A %s %s.", noun, surname);

        PersonSurnameFinder personSurnameFinder = new PersonSurnameFinder();
        List<Immutable> matches = personSurnameFinder.findList(text);

        Set<String> expected = Collections.singleton(surname);
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }
}
