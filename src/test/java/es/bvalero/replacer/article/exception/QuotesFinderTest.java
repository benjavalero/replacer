package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class QuotesFinderTest {

    @Test
    public void testRegexSingleQuotes() {
        String quotes1 = "''tt''";
        String quotes2 = "''y '''á''' y''";
        String quotes3 = "''zz\n";
        String quotes4 = "'''zzz'''";
        String quotes5 = "'''ccc ''dd'' ee'''";
        String quotes6 = "'''''bbb'''''";

        String text = "xxx " + quotes1 + " / " + quotes2 + " / " + quotes3 + " / " + quotes4 + " / " + quotes5 + " / " + quotes6 + ".";

        QuotesFinder quotesFinder = new QuotesFinder();

        List<RegexMatch> matches = quotesFinder.findExceptionMatches(text, false);

        Assert.assertEquals(6, matches.size());
        Assert.assertEquals(quotes1, matches.get(0).getOriginalText());
        Assert.assertEquals(quotes2, matches.get(1).getOriginalText());
        Assert.assertEquals("''dd''", matches.get(2).getOriginalText());
        Assert.assertEquals(quotes4, matches.get(3).getOriginalText());
        Assert.assertEquals(quotes5, matches.get(4).getOriginalText());
        Assert.assertEquals(quotes6, matches.get(5).getOriginalText());

        matches = quotesFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(5, matches.size());
        Assert.assertEquals(StringUtils.escapeText(quotes1), matches.get(0).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(quotes2), matches.get(1).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(quotes4), matches.get(2).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(quotes5), matches.get(3).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(quotes6), matches.get(4).getOriginalText());
    }

    @Test
    public void testRegexQuotesAngular() {
        String quotes1 = "«yáy»";
        String quotes2 = "«z, zz»";
        String quotes3 = "«z\nz»";
        String text = "xxx " + quotes1 + " / " + quotes2 + " /" + quotes3 + ".";

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
    public void testRegexQuotesTypographic() {
        String quotes1 = "“yáy”";
        String quotes2 = "“z, zz”";
        String quotes3 = "“z\nz”";
        String text = "xxx " + quotes1 + " / " + quotes2 + " /" + quotes3 + ".";

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
    public void testRegexDoubleQuotes() {
        String quotes1 = "\"yáy\"";
        String quotes2 = "\"zzz\"";
        String quotes3 = "\"z\nz\"";
        String text = "xxx " + quotes1 + " / " + quotes2 + " /" + quotes3 + ".";

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
