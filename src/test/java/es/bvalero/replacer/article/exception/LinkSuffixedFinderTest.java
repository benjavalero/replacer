package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class LinkSuffixedFinderTest {

    @Test
    public void testRegexUrl() {
        String url = "[[brasil]]eño";
        String text = "xxx " + url + " zzz";

        LinkSuffixedFinder linkSuffixedFinder = new LinkSuffixedFinder();

        List<RegexMatch> matches = linkSuffixedFinder.findExceptionMatches(text, false);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(url, matches.get(0).getOriginalText());

        matches = linkSuffixedFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(StringUtils.escapeText(url), matches.get(0).getOriginalText());
    }

}
