package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class QuotesFinderTest {

    @Test
    public void testRegexQuotes() {
        String text = "xxx '''I'm Muzzy''' \"zzz\" ''''ttt'' ''uuu\" vvv";
        QuotesMatchFinder quotesFinder = new QuotesMatchFinder();
        List<RegexMatch> matches = quotesFinder.findExceptionMatches(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(3, matches.size());
        Assert.assertTrue(matches.contains(new RegexMatch(4, "'''I'm Muzzy'''")));
        Assert.assertTrue(matches.contains(new RegexMatch(20, "\"zzz\"")));
        Assert.assertTrue(matches.contains(new RegexMatch(26, "''''ttt''")));
    }

    @Test
    public void testRegexQuotesEscaped() {
        String text = "xxx '''I'm Muzzy''' \"zzz\" ''''ttt'' ''uuu\" vvv";
        QuotesMatchFinder quotesFinder = new QuotesMatchFinder();
        List<RegexMatch> matches = quotesFinder.findExceptionMatches(StringUtils.escapeText(text));

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(3, matches.size());
        Assert.assertTrue(matches.contains(new RegexMatch(4, StringUtils.escapeText("'''I'm Muzzy'''"))));
        Assert.assertTrue(matches.contains(new RegexMatch(55, StringUtils.escapeText("\"zzz\""))));
        Assert.assertTrue(matches.contains(new RegexMatch(71, StringUtils.escapeText("''''ttt''"))));
    }


    @Test
    public void testRegexQuotesAngular() {
        String quotes = "«yyy»";
        String text = "xxx " + quotes + " zzz";

        QuotesMatchFinder quotesFinder = new QuotesMatchFinder();
        List<RegexMatch> matches = quotesFinder.findExceptionMatches(StringUtils.escapeText(text));

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(quotes, matches.get(0).getOriginalText());
    }

    @Test
    public void testRegexQuotesTypographic() {
        String quotes = "“yyy”";
        String text = "xxx " + quotes + " zzz";

        QuotesMatchFinder quotesFinder = new QuotesMatchFinder();
        List<RegexMatch> matches = quotesFinder.findExceptionMatches(StringUtils.escapeText(text));

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(quotes, matches.get(0).getOriginalText());
    }

}
