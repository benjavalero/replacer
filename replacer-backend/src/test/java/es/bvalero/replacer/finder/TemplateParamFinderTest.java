package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TemplateParamFinderTest {

    @Test
    public void testRegexTemplateParam() {
        String param1 = " param 1 ";
        String param2 = "\tparám_2\t";
        String param3 = "param-3";
        String param4 = "param4";
        String link = "[[A|B]]\n==Section==";
        String text = String.format("{{Template|%s= value1 |%s= value2 |%s|%s=}} %s", param1, param2, param3, param4, link);

        IgnoredReplacementFinder templateParamFinder = new TemplateParamFinder();

        List<IgnoredReplacement> matches = templateParamFinder.findIgnoredReplacements(text);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(param1, matches.get(0).getText());
        Assert.assertEquals(param2, matches.get(1).getText());
        Assert.assertEquals(param4, matches.get(2).getText());
    }

}
