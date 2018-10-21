package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.IgnoredReplacementFinder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UrlFinderTest {

    @Test
    public void testRegexUrl() {
        String url = "https://google.es?u=aj&t2+rl=http://www.marca.com#!page~2,3";
        String text = "xxx " + url + " zzz";

        IgnoredReplacementFinder urlFinder = new UrlFinder();

        List<ArticleReplacement> matches = urlFinder.findIgnoredReplacements(text);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(url, matches.get(0).getText());
    }

    @Test
    public void testRegexDomain() {
        String domain1 = "www.google.es";
        String domain2 = "IMDb.org";
        String domain3 = "BBC.co.uk";
        String text = "xxx " + domain1 + " / " + domain2 + " / " + domain3 + " zzz";

        IgnoredReplacementFinder urlFinder = new UrlFinder();

        List<ArticleReplacement> matches = urlFinder.findIgnoredReplacements(text);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(domain1, matches.get(0).getText());
        Assert.assertEquals(domain2, matches.get(1).getText());
    }

}
