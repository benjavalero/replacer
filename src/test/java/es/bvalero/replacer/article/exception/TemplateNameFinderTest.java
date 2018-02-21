package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TemplateNameFinderTest {

    @Test
    public void testRegexTemplateParam() {
        String text = "xxx {{Plantilla| yyy }} zzz";

        TemplateNameMatchFinder templateNameFinder = new TemplateNameMatchFinder();
        List<RegexMatch> matches = templateNameFinder.findExceptionMatches(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(1, matches.size());
        Assert.assertEquals("{{Plantilla", matches.get(0).getOriginalText());
    }

}
