package es.bvalero.replacer.finder.ignored;

import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.finder.MatchResult;
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

        String text = "xxx " + quotes1 + " / " + quotes2 + " / " + quotes3 + " / " + quotes4 + " / " + quotes5 + " / " + quotes6 + '.';

        IgnoredReplacementFinder quotesFinder = new QuotesFinder();

        List<MatchResult> matches = quotesFinder.findIgnoredReplacements(text);

        Assert.assertEquals(6, matches.size());
        Assert.assertEquals(quotes1, matches.get(0).getText());
        Assert.assertEquals(quotes2, matches.get(1).getText());
        Assert.assertEquals("''dd''", matches.get(2).getText());
        Assert.assertEquals(quotes4, matches.get(3).getText());
        Assert.assertEquals(quotes5, matches.get(4).getText());
        Assert.assertEquals(quotes6, matches.get(5).getText());
    }

    @Test
    public void testRegexQuotesAngular() {
        String quotes1 = "«yáy»";
        String quotes2 = "«z, zz»";
        String quotes3 = "«z\nz»";
        String text = "xxx " + quotes1 + " / " + quotes2 + " /" + quotes3 + '.';

        IgnoredReplacementFinder quotesFinder = new QuotesFinder();

        List<MatchResult> matches = quotesFinder.findIgnoredReplacements(text);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(quotes1, matches.get(0).getText());
        Assert.assertEquals(quotes2, matches.get(1).getText());
        Assert.assertEquals(quotes3, matches.get(2).getText());
    }

    @Test
    public void testRegexQuotesTypographic() {
        String quotes1 = "“yáy”";
        String quotes2 = "“z, zz”";
        String quotes3 = "“z\nz”";
        String text = "xxx " + quotes1 + " / " + quotes2 + " /" + quotes3 + '.';

        IgnoredReplacementFinder quotesFinder = new QuotesFinder();

        List<MatchResult> matches = quotesFinder.findIgnoredReplacements(text);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(quotes1, matches.get(0).getText());
        Assert.assertEquals(quotes2, matches.get(1).getText());
        Assert.assertEquals(quotes3, matches.get(2).getText());
    }

    @Test
    public void testRegexDoubleQuotes() {
        String quotes1 = "\"yáy\"";
        String quotes2 = "\"zzz\"";
        String quotes3 = "\"z\nz\"";
        String text = "xxx " + quotes1 + " / " + quotes2 + " /" + quotes3 + '.';

        IgnoredReplacementFinder quotesFinder = new QuotesFinder();

        List<MatchResult> matches = quotesFinder.findIgnoredReplacements(text);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(quotes1, matches.get(0).getText());
        Assert.assertEquals(quotes2, matches.get(1).getText());
    }

}
