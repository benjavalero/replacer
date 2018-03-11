package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class QuotesFinderTest {

    @Test
    public void testRegexSingleQuotes() {
        String quotes1 = "''y '''á''' y''";
        String quotes2 = "'''zzz'''";
        String quotes3 = "''tt''";
        String text = "xxx " + quotes1 + " / " + quotes2 + " / " + quotes3 + ".";

        QuotesFinder quotesFinder = new QuotesFinder();

        List<RegexMatch> matches = quotesFinder.findExceptionMatches(text, false);

        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(quotes1, matches.get(0).getOriginalText());
        Assert.assertEquals(quotes2, matches.get(1).getOriginalText());
        Assert.assertEquals(quotes3, matches.get(2).getOriginalText());

        matches = quotesFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(StringUtils.escapeText(quotes1), matches.get(0).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(quotes2), matches.get(1).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(quotes3), matches.get(2).getOriginalText());
    }

    @Test
    public void testRegexQuotesAngular() {
        String quotes1 = "«yáy»";
        String quotes2 = "«zzz»";
        String text = "xxx " + quotes1 + " / " + quotes2 + ".";

        QuotesFinder quotesFinder = new QuotesFinder();

        List<RegexMatch> matches = quotesFinder.findExceptionMatches(text, false);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(quotes1, matches.get(0).getOriginalText());
        Assert.assertEquals(quotes2, matches.get(1).getOriginalText());

        matches = quotesFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(StringUtils.escapeText(quotes1), matches.get(0).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(quotes2), matches.get(1).getOriginalText());
    }

    @Test
    public void testRegexQuotesTypographic() {
        String quotes1 = "“yáy”";
        String quotes2 = "“zzz”";
        String text = "xxx " + quotes1 + " / " + quotes2 + ".";

        QuotesFinder quotesFinder = new QuotesFinder();

        List<RegexMatch> matches = quotesFinder.findExceptionMatches(text, false);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(quotes1, matches.get(0).getOriginalText());
        Assert.assertEquals(quotes2, matches.get(1).getOriginalText());

        matches = quotesFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(StringUtils.escapeText(quotes1), matches.get(0).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(quotes2), matches.get(1).getOriginalText());
    }

    @Test
    public void testRegexDoubleQuotes() {
        String quotes1 = "\"yáy\"";
        String quotes2 = "\"zzz\"";
        String text = "xxx " + quotes1 + " / " + quotes2 + ".";

        QuotesFinder quotesFinder = new QuotesFinder();

        List<RegexMatch> matches = quotesFinder.findExceptionMatches(text, false);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(quotes1, matches.get(0).getOriginalText());
        Assert.assertEquals(quotes2, matches.get(1).getOriginalText());

        matches = quotesFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(StringUtils.escapeText(quotes1), matches.get(0).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(quotes2), matches.get(1).getOriginalText());
    }

}
