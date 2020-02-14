package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.finder.Immutable;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

        List<Immutable> matches = personNameFinder.findList(text);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(1, matches.size());
        Assert.assertEquals(noun, matches.get(0).getText());
        Assert.assertEquals(2, matches.get(0).getStart()); // Only the first one
    }
}
