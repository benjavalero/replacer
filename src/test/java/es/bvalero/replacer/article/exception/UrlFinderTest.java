package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UrlFinderTest {

    @Test
    public void testRegexUrl() {
        String url = "https://google.es?u=aj&t2+rl=http://www.marca.com#!page~2,3";
        String text = "xxx " + url + " zzz";

        UrlFinder urlFinder = new UrlFinder();

        List<RegexMatch> matches = urlFinder.findExceptionMatches(text, false);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(url, matches.get(0).getOriginalText());

        matches = urlFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(StringUtils.escapeText(url), matches.get(0).getOriginalText());
    }

    @Test
    public void testRegexDomain() {
        String domain1 = "google.es";
        String domain2 = "IMDb.org";
        String text = "xxx " + domain1 + " / " + domain2 + " zzz";

        UrlFinder urlFinder = new UrlFinder();

        List<RegexMatch> matches = urlFinder.findExceptionMatches(text, false);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(domain1, matches.get(0).getOriginalText());
        Assert.assertEquals(domain2, matches.get(1).getOriginalText());

        matches = urlFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(domain1, matches.get(0).getOriginalText());
        Assert.assertEquals(domain2, matches.get(1).getOriginalText());
    }

}
