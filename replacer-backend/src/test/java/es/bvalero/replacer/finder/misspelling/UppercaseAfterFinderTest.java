package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.finder.Immutable;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UppercaseAfterFinderTest {
    private UppercaseAfterFinder uppercaseAfterFinder;

    @Before
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
        Misspelling misspelling5 = Misspelling.of("Julio", true, "Julio, julio"); // Ignored
        Misspelling misspelling6 = Misspelling.of("Paris", true, "París"); // Ignored
        Set<Misspelling> misspellingSet = new HashSet<>(
            Arrays.asList(misspelling1, misspelling2, misspelling3, misspelling4, misspelling5, misspelling6)
        );

        // Fake the update of the misspelling list in the misspelling manager
        uppercaseAfterFinder.propertyChange(
            new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, misspellingSet)
        );

        List<Immutable> matches = uppercaseAfterFinder.findList(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(noun1, matches.get(0).getText());
        Assert.assertEquals(9, matches.get(0).getStart());
        Assert.assertEquals(noun2, matches.get(1).getText());
        Assert.assertEquals(17, matches.get(1).getStart());
    }
}
