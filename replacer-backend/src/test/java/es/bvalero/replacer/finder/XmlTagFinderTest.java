package es.bvalero.replacer.finder;

import es.bvalero.replacer.finder2.Immutable;
import es.bvalero.replacer.finder2.ImmutableFinder;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class XmlTagFinderTest {

    @Test
    public void testXmlTagFinder() {
        String tag1 = "<span style=\"color:green;\">";
        String tag2 = "</span>";
        String tag3 = "<br />";
        String text = String.format("%s %s %s", tag1, tag2, tag3);

        ImmutableFinder xmlTagFinder = new XmlTagFinder();

        List<Immutable> matches = xmlTagFinder.findList(text);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(tag1, matches.get(0).getText());
        Assert.assertEquals(tag2, matches.get(1).getText());
        Assert.assertEquals(tag3, matches.get(2).getText());
    }

    @Test
    public void testRegexCommentNotMatched() {
        String comment = "<!-- Esto es un comentario -->";
        String text = "xxx " + comment + " zzz";

        ImmutableFinder xmlTagFinder = new XmlTagFinder();

        List<Immutable> matches = xmlTagFinder.findList(text);
        Assert.assertTrue(matches.isEmpty());
    }
}
