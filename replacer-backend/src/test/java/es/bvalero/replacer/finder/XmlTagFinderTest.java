package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class XmlTagFinderTest {

    @Test
    public void testXmlTagFinder() {
        String tag1 = "<span style=\"color:green;\">";
        String tag2 = "</span>";
        String tag3 = "<br />";
        String text = String.format("%s %s %s", tag1, tag2, tag3);

        IgnoredReplacementFinder xmlTagFinder = new XmlTagFinder();

        List<MatchResult> matches = xmlTagFinder.findIgnoredReplacements(text);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(tag1, matches.get(0).getText());
        Assert.assertEquals(tag2, matches.get(1).getText());
        Assert.assertEquals(tag3, matches.get(2).getText());
    }

    @Test
    public void testRegexCommentNotMatched() {
        String comment = "<!-- Esto es un comentario -->";
        String text = "xxx " + comment + " zzz";

        IgnoredReplacementFinder xmlTagFinder = new XmlTagFinder();

        List<MatchResult> matches = xmlTagFinder.findIgnoredReplacements(text);
        Assert.assertTrue(matches.isEmpty());
    }

}
