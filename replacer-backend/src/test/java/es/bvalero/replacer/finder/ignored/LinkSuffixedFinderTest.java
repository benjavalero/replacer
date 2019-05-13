package es.bvalero.replacer.finder.ignored;

import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.finder.MatchResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class LinkSuffixedFinderTest {

    @Test
    public void testRegexUrl() {
        String suffixed1 = "[[brasil]]eño";
        String suffixed2 = "|reaccion]]es";
        String link2 = "[[reacción química" + suffixed2;
        String text = "xxx " + suffixed1 + " yyy " + link2 + " zzz";

        IgnoredReplacementFinder linkSuffixedFinder = new LinkSuffixedFinder();

        List<MatchResult> matches = linkSuffixedFinder.findIgnoredReplacements(text);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(suffixed1, matches.get(0).getText());
        Assert.assertEquals(suffixed2, matches.get(1).getText());
    }

}
