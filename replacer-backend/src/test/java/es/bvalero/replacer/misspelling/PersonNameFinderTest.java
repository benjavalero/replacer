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
        String text = "xxx " + noun + ' ' + surname + " zzz";

        List<IgnoredReplacement> matches = personNameFinder.findIgnoredReplacements(text);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(1, matches.size());
        Assert.assertEquals(noun, matches.get(0).getText());
    }

}
