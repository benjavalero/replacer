package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.finder.Cosmetic;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class SameLinkFinderTest {
    private SameLinkFinder sameLinkFinder = new SameLinkFinder();

    @Test
    public void testSameLinkFinder() {
        String link1 = "[[test|test]]";
        String link2 = "[[Test|test]]";
        String link3 = "[[test|Test]]";
        String link4 = "[[Test|Test]]";
        String link5 = "[[Test|Mock]]";
        String text = String.format("En %s %s %s %s %s.", link1, link2, link3, link4, link5);

        List<Cosmetic> replacements = sameLinkFinder.findList(text);

        String expectedLink = "[[test]]";
        Assert.assertEquals(3, replacements.size());
        Assert.assertEquals(link1, replacements.get(0).getText());
        Assert.assertEquals(expectedLink, replacements.get(0).getFix());
        Assert.assertEquals(link2, replacements.get(1).getText());
        Assert.assertEquals(expectedLink, replacements.get(1).getFix());

        Assert.assertEquals(link4, replacements.get(2).getText());
        Assert.assertEquals("[[Test]]", replacements.get(2).getFix());
    }
}
