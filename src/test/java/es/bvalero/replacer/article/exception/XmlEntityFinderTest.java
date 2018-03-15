package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class XmlEntityFinderTest {

    @Test
    public void testRegexXmlEntity() {
        String entity1 = "<";
        String entity2 = "&";
        String text = "xxx " + entity1 + " / " + entity2 + " zzz";

        XmlEntityFinder xmlEntityFinder = new XmlEntityFinder();

        List<RegexMatch> matches = xmlEntityFinder.findExceptionMatches(text, false);
        Assert.assertTrue(matches.isEmpty());

        matches = xmlEntityFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(StringUtils.escapeText(entity1), matches.get(0).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(entity2), matches.get(1).getOriginalText());
    }

}
