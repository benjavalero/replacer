package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UrlFinderTest {

    @Test
    public void testRegexUrl() {
        String url = "https://google.es?u=t2+rl=http://www.marca.com#!page~2,3";
        String text = "xxx " + url + " zzz";

        UrlFinder urlFinder = new UrlFinder();

        List<RegexMatch> matches = urlFinder.findExceptionMatches(text, false);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(url, matches.get(0).getOriginalText());

        matches = urlFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(url, matches.get(0).getOriginalText());
    }

    @Test
    public void testRegexDomain() {
        String url = "google.com";
        String text = "xxx " + url + " zzz";

        UrlFinder urlFinder = new UrlFinder();

        List<RegexMatch> matches = urlFinder.findExceptionMatches(text, false);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(url, matches.get(0).getOriginalText());

        matches = urlFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(url, matches.get(0).getOriginalText());
    }

}
