package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class XmlTagFinderTest {

    @Test
    public void testRegexRefName() {
        String ref1 = "<ref name=\"España\">";
        String ref2 = "</ref>";
        String ref3 = "<ref />";
        String text = "xxx " + ref1 + " zzz " + ref2 + " / " + ref3 + ".";

        XmlTagFinder xmlTagFinder = new XmlTagFinder();

        List<RegexMatch> matches = xmlTagFinder.findExceptionMatches(text, false);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(ref1, matches.get(0).getOriginalText());
        Assert.assertEquals(ref2, matches.get(1).getOriginalText());
        Assert.assertEquals(ref3, matches.get(2).getOriginalText());

        matches = xmlTagFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(StringUtils.escapeText(ref1), matches.get(0).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(ref2), matches.get(1).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(ref3), matches.get(2).getOriginalText());
    }

    @Test
    public void testRegexCommentNotMatched() {
        String comment = "<!-- Esto es un comentario -->";
        String text = "xxx " + comment + " zzz";

        XmlTagFinder xmlTagFinder = new XmlTagFinder();

        List<RegexMatch> matches = xmlTagFinder.findExceptionMatches(text, false);
        Assert.assertTrue(matches.isEmpty());

        matches = xmlTagFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertTrue(matches.isEmpty());
    }

}
