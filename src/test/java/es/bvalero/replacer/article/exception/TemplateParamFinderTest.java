package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TemplateParamFinderTest {

    @Test
    public void testRegexTemplateParam() {
        String param1 = "| param 1 =";
        String param2 = "| parám_2 =";
        String param3 = "|param-3=";
        String param4 = "| param4";
        String text = "xxx {{Template" + param1 + " value1 " + param2 + " value2 " + param3 + param4 + "}}";

        TemplateParamFinder templateParamFinder = new TemplateParamFinder();

        List<RegexMatch> matches = templateParamFinder.findExceptionMatches(text, false);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(param1, matches.get(0).getOriginalText());
        Assert.assertEquals(param2, matches.get(1).getOriginalText());
        Assert.assertEquals(param3, matches.get(2).getOriginalText());

        matches = templateParamFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(StringUtils.escapeText(param1), matches.get(0).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(param2), matches.get(1).getOriginalText());
        Assert.assertEquals(StringUtils.escapeText(param3), matches.get(2).getOriginalText());
    }

}
