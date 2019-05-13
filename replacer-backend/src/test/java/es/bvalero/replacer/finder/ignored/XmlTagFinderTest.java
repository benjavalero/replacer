package es.bvalero.replacer.finder.ignored;

import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.finder.MatchResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class XmlTagFinderTest {

    @Test
    public void testRegexRefName() {
        String ref1 = "<ref name=\"España\">";
        String ref2 = "</ref>";
        String ref3 = "<ref />";
        String text = "xxx " + ref1 + " zzz " + ref2 + " / " + ref3 + '.';

        IgnoredReplacementFinder xmlTagFinder = new XmlTagFinder();

        List<MatchResult> matches = xmlTagFinder.findIgnoredReplacements(text);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(ref1, matches.get(0).getText());
        Assert.assertEquals(ref2, matches.get(1).getText());
        Assert.assertEquals(ref3, matches.get(2).getText());
    }

    @Test
    public void testRegexSpan() {
        String span1 = "<span style=\"color:green;\">";
        String span2 = "</span>";
        String text = "xxx " + span1 + "zzz" + span2 + '.';

        IgnoredReplacementFinder xmlTagFinder = new XmlTagFinder();

        List<MatchResult> matches = xmlTagFinder.findIgnoredReplacements(text);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(span1, matches.get(0).getText());
        Assert.assertEquals(span2, matches.get(1).getText());
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
