package es.bvalero.replacer.finder.ignored;

import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.finder.MatchResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UrlFinderTest {

    @Test
    public void testRegexUrl() {
        String url = "https://google.es?u=aj&t2+rl=http://www.marca.com#!page~2,3";
        String text = "xxx " + url + " zzz";

        IgnoredReplacementFinder urlFinder = new UrlFinder();

        List<MatchResult> matches = urlFinder.findIgnoredReplacements(text);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(url, matches.get(0).getText());
    }

}
