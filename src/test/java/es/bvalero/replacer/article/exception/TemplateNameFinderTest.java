package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TemplateNameFinderTest {

    @Test
    public void testRegexTemplateParam() {
        String text = "xxx {{Plantilla1| yyy }} zzz {{Plantilla2}}.";

        TemplateNameFinder templateNameFinder = new TemplateNameFinder();

        List<RegexMatch> matches = templateNameFinder.findExceptionMatches(text, false);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals("{{Plantilla1", matches.get(0).getOriginalText());
        Assert.assertEquals("{{Plantilla2", matches.get(1).getOriginalText());

        matches = templateNameFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals("{{Plantilla1", matches.get(0).getOriginalText());
        Assert.assertEquals("{{Plantilla2", matches.get(1).getOriginalText());
    }

}
