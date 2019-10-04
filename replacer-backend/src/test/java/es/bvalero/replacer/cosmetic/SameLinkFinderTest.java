package es.bvalero.replacer.cosmetic;

import es.bvalero.replacer.finder.Replacement;
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

        List<Replacement> replacements = sameLinkFinder.findReplacements(text);

        String expectedLink = "[[test]]";
        Assert.assertEquals(3, replacements.size());
        Assert.assertEquals(link1, replacements.get(0).getText());
        Assert.assertEquals(expectedLink, replacements.get(0).getSuggestions().get(0).getText());
        Assert.assertEquals(link2, replacements.get(1).getText());
        Assert.assertEquals(expectedLink, replacements.get(1).getSuggestions().get(0).getText());

        Assert.assertEquals(link4, replacements.get(2).getText());
        Assert.assertEquals("[[Test]]", replacements.get(2).getSuggestions().get(0).getText());
    }

}
