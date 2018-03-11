package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TemplateParamFinderTest {

    @Test
    public void testRegexTemplateParam() {
        String text = "xxx {{Template| param1 = value1 | parám_ 2 = value2|param-3=|param4 }} {{Cita|Alea iacta est}} jajaja =";

        TemplateParamFinder templateParamFinder = new TemplateParamFinder();

        List<RegexMatch> matches = templateParamFinder.findExceptionMatches(text, false);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(3, matches.size());
        Assert.assertTrue(matches.contains(new RegexMatch(14, "| param1 =")));
        Assert.assertTrue(matches.contains(new RegexMatch(32, "| parám_ 2 =")));
        Assert.assertTrue(matches.contains(new RegexMatch(51, "|param-3=")));

        matches = templateParamFinder.findExceptionMatches(StringUtils.escapeText(text), true);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(3, matches.size());
        Assert.assertTrue(matches.contains(new RegexMatch(14, "| param1 =")));
        Assert.assertTrue(matches.contains(new RegexMatch(32, "| parám_ 2 =")));
        Assert.assertTrue(matches.contains(new RegexMatch(51, "|param-3=")));
    }

}
