package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class LinkSuffixedFinderTest {

    @Test
    public void testRegexUrl() {
        String suffixed1 = "[[brasil]]eño";
        String suffixed2 = "[[reacción química|reaccion]]es";
        String noSuffixed = "[[Text]]";
        String text = String.format("%s %s %s.", suffixed1, suffixed2, noSuffixed);

        IgnoredReplacementFinder linkSuffixedFinder = new LinkSuffixedFinder();

        List<MatchResult> matches = linkSuffixedFinder.findIgnoredReplacements(text);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(suffixed1, matches.get(0).getText());
        Assert.assertEquals(suffixed2, matches.get(1).getText());
    }

}
