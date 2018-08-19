package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TemplateNameFinderTest {

    @Test
    public void testRegexTemplateParam() {
        String template1 = "{{Plantilla 1";
        String template2 = "{{Plantilla\n 2";
        String template3 = "{{Plantilla-3";

        String text = "xxx " + template1 + "| yyy }} / " + template2 + "}} / " + template3 + ":zzz}}.";

        TemplateNameFinder templateNameFinder = new TemplateNameFinder();

        List<RegexMatch> matches = templateNameFinder.findExceptionMatches(text, false);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(template1, matches.get(0).getOriginalText());
        Assert.assertEquals(template2, matches.get(1).getOriginalText());
        Assert.assertEquals(template3, matches.get(2).getOriginalText());

        matches = templateNameFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(StringUtils.escapeText(template1), matches.get(0).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(template2), matches.get(1).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(template3), matches.get(2).getOriginalText());
    }

}
