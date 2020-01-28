package es.bvalero.replacer.finder;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class QuotesFinderTest {

    @Test
    public void testRegexQuotesAngular() {
        String quotes1 = "«yáy»";
        String quotes2 = "«z, zz»";
        String quotes3 = "«z\nz»";
        String text = String.format("%s %s %s.", quotes1, quotes2, quotes3);

        ImmutableFinder quotesFinder = new QuotesFinder();

        List<Immutable> matches = quotesFinder.findList(text);
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

        ImmutableFinder quotesFinder = new QuotesFinder();

        List<Immutable> matches = quotesFinder.findList(text);
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

        ImmutableFinder quotesFinder = new QuotesFinder();

        List<Immutable> matches = quotesFinder.findList(text);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(quotes1, matches.get(0).getText());
        Assert.assertEquals(quotes2, matches.get(1).getText());
    }
}
