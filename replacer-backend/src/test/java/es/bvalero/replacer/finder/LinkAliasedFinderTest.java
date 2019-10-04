package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class LinkAliasedFinderTest {

    @Test
    public void testRegexUrl() {
        String aliased1 = "[[brasil|";
        String aliased2 = "[[reacción química|";
        String noAliased = "[[Text]]";
        String text = String.format("%sBrasil]] %sreacción]] %s.", aliased1, aliased2, noAliased);

        IgnoredReplacementFinder linkAliasedFinder = new LinkAliasedFinder();

        List<IgnoredReplacement> matches = linkAliasedFinder.findIgnoredReplacements(text);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(aliased1, matches.get(0).getText());
        Assert.assertEquals(aliased2, matches.get(1).getText());
    }

}
