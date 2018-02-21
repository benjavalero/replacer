package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class XmlTagFinderTest {

    @Test
    public void testRegexRefName() {
        String ref = "<ref  name= España >";
        String text = "xxx " + ref + " zzz";

        XmlTagMatchFinder xmlTagFinder = new XmlTagMatchFinder();
        List<RegexMatch> matches = xmlTagFinder.findExceptionMatches(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(ref, matches.get(0).getOriginalText());
    }

    @Test
    public void testRegexRefNameEscaped() {
        String ref = "<ref  name  =España />";
        String text = "xxx " + ref + " zzz";

        XmlTagMatchFinder xmlTagFinder = new XmlTagMatchFinder();
        List<RegexMatch> matches = xmlTagFinder.findExceptionMatches(StringUtils.escapeText(text));

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(StringUtils.escapeText(ref), matches.get(0).getOriginalText());
    }

    @Test
    public void testRegexComment() {
        String comment = "<!-- Esto es un \n comentario -->";
        String text = "xxx " + comment + " zzz";

        XmlTagMatchFinder xmlTagFinder = new XmlTagMatchFinder();
        List<RegexMatch> matches = xmlTagFinder.findExceptionMatches(text);

        Assert.assertTrue(matches.isEmpty());
    }

    @Test
    public void testRegexCommentEscaped() {
        String comment = "<!-- Esto es un \n comentario -->";
        String text = "xxx " + comment + " zzz";

        XmlTagMatchFinder xmlTagFinder = new XmlTagMatchFinder();
        List<RegexMatch> matches = xmlTagFinder.findExceptionMatches(StringUtils.escapeText(text));

        Assert.assertTrue(matches.isEmpty());
    }

}
