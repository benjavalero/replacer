package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.finder.IgnoredReplacement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class PersonNameFinderTest {

    private PersonNameFinder personNameFinder;

    @Before
    public void setUp() {
        personNameFinder = new PersonNameFinder();
    }

    @Test
    public void testRegexPersonName() {
        String noun = "Julio";
        String surname = "Verne";
        String text = String.format("A %s %s %ss %s %s %s.", noun, surname, noun, noun, surname.toLowerCase(), noun);

        List<IgnoredReplacement> matches = personNameFinder.findIgnoredReplacements(text);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(1, matches.size());
        Assert.assertEquals(noun, matches.get(0).getText());
        Assert.assertEquals(2, matches.get(0).getStart()); // Only the first one
    }

}
