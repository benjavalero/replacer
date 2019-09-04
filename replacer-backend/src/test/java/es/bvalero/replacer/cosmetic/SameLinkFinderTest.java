package es.bvalero.replacer.cosmetic;

import es.bvalero.replacer.finder.ArticleReplacement;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SameLinkFinderTest {

    private SameLinkFinder sameLinkFinder = new SameLinkFinder();

    @Test
    public void testSameLinkFinder() {
        String link1 = "[[test|test]]";
        String link2 = "[[Test|test]]";
        String link3 = "[[test|Test]]";
        String link4 = "[[Test|Test]]";
        String text = String.format("En %s %s %s %s.", link1, link2, link3, link4);

        List<ArticleReplacement> articleReplacements = sameLinkFinder.findReplacements(text);

        String expectedLink = "[[test]]";
        Assert.assertEquals(3, articleReplacements.size());
        Assert.assertEquals(link1, articleReplacements.get(0).getText());
        Assert.assertEquals(expectedLink, articleReplacements.get(0).getSuggestions().get(0).getText());
        Assert.assertEquals(link2, articleReplacements.get(1).getText());
        Assert.assertEquals(expectedLink, articleReplacements.get(1).getSuggestions().get(0).getText());

        Assert.assertEquals(link4, articleReplacements.get(2).getText());
        Assert.assertEquals("[[Test]]", articleReplacements.get(2).getSuggestions().get(0).getText());
    }

}
