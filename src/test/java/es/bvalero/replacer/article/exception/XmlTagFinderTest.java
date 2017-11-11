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

        XmlTagFinder xmlTagFinder = new XmlTagFinder();
        List<RegexMatch> matches = xmlTagFinder.findErrorExceptions(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(ref, matches.get(0).getOriginalText());
    }

    @Test
    public void testRegexRefNameEscaped() {
        String ref = "<ref  name  =España />";
        String text = "xxx " + ref + " zzz";

        XmlTagFinder xmlTagFinder = new XmlTagFinder();
        List<RegexMatch> matches = xmlTagFinder.findErrorExceptions(StringUtils.escapeText(text));

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(StringUtils.escapeText(ref), matches.get(0).getOriginalText());
    }

    @Test
    public void testRegexComment() {
        String comment = "<!-- Esto es un \n comentario -->";
        String text = "xxx " + comment + " zzz";

        XmlTagFinder xmlTagFinder = new XmlTagFinder();
        List<RegexMatch> matches = xmlTagFinder.findErrorExceptions(text);

        Assert.assertTrue(matches.isEmpty());
    }

    @Test
    public void testRegexCommentEscaped() {
        String comment = "<!-- Esto es un \n comentario -->";
        String text = "xxx " + comment + " zzz";

        XmlTagFinder xmlTagFinder = new XmlTagFinder();
        List<RegexMatch> matches = xmlTagFinder.findErrorExceptions(StringUtils.escapeText(text));

        Assert.assertTrue(matches.isEmpty());
    }

}
