package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.finder.IgnoredReplacement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.util.*;

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
        Set<Misspelling> misspellingSet = new HashSet<>(Arrays.asList(misspelling1, misspelling2));

        // Fake the update of the misspelling list in the misspelling manager
        uppercaseAfterFinder.propertyChange(new PropertyChangeEvent(this, "name", Collections.EMPTY_SET, misspellingSet));

        List<IgnoredReplacement> matches = uppercaseAfterFinder.findIgnoredReplacements(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(noun1, matches.get(0).getText());
        Assert.assertEquals(9, matches.get(0).getStart());
        Assert.assertEquals(noun2, matches.get(1).getText());
        Assert.assertEquals(17, matches.get(1).getStart());
    }

}
