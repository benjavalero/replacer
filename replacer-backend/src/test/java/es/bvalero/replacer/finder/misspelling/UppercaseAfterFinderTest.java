package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.finder.Immutable;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UppercaseAfterFinderTest {
    private UppercaseAfterFinder uppercaseAfterFinder;

    @BeforeEach
    public void setUp() {
        uppercaseAfterFinder = new UppercaseAfterFinder();
    }

    @Test
    public void testRegexUppercaseAfter() {
        String noun1 = "Enero";
        String noun2 = "Febrero";
        String text = "{{ param=" + noun1 + " | " + noun2 + " }} zzz";

        Misspelling misspelling1 = Misspelling.of("Enero", true, "enero");
        Misspelling misspelling2 = Misspelling.of("Febrero", true, "febrero");
        Misspelling misspelling3 = Misspelling.of("habia", false, "había"); // Ignored
        Misspelling misspelling4 = Misspelling.of("madrid", true, "Madrid"); // Ignored
        Misspelling misspelling5 = Misspelling.of("Julio", true, "Julio, julio");
        Misspelling misspelling6 = Misspelling.of("Paris", true, "París"); // Ignored
        Set<Misspelling> misspellingSet = new HashSet<>(
            Arrays.asList(misspelling1, misspelling2, misspelling3, misspelling4, misspelling5, misspelling6)
        );

        // Fake the update of the misspelling list in the misspelling manager
        uppercaseAfterFinder.propertyChange(
            new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, misspellingSet)
        );

        Set<String> expectedWords = new HashSet<>(
            Arrays.asList(misspelling1.getWord(), misspelling2.getWord(), misspelling5.getWord())
        );
        Assertions.assertEquals(expectedWords, new HashSet<>(uppercaseAfterFinder.getUppercaseWords()));

        List<Immutable> matches = uppercaseAfterFinder.findList(text);

        Set<String> expected = new HashSet<>(Arrays.asList(noun1, noun2));
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }
}
