package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.utils.RegexMatch;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TemplateParamFinderTest {

    @Test
    public void testRegexTemplateParam() {
        String text = "xxx {{Template| param1 = value1 | parám_ 2 = value2|param-3=|param4 }} {{Cita|Alea iacta est}} jajaja =";

        TemplateParamMatchFinder templateParamFinder = new TemplateParamMatchFinder();
        List<RegexMatch> matches = templateParamFinder.findExceptionMatches(text);

        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(3, matches.size());
        Assert.assertTrue(matches.contains(new RegexMatch(14, "| param1 =")));
        Assert.assertTrue(matches.contains(new RegexMatch(32, "| parám_ 2 =")));
        Assert.assertTrue(matches.contains(new RegexMatch(51, "|param-3=")));
    }

}
