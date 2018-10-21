package es.bvalero.replacer.article.exception;

import es.bvalero.replacer.article.ArticleReplacement;
import es.bvalero.replacer.article.IgnoredReplacementFinder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TemplateParamFinderTest {

    @Test
    public void testRegexTemplateParam() {
        String param1 = "param 1";
        String param2 = "parám_2";
        String param3 = "param-3";
        String param4 = "param4";
        String text = "xxx {{Template| " + param1 + "= value1 | " + param2 + "= value2 |" + param3 + "=| " + param4 + "}}";

        IgnoredReplacementFinder templateParamFinder = new TemplateParamFinder();

        List<ArticleReplacement> matches = templateParamFinder.findIgnoredReplacements(text);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(param1, matches.get(0).getText());
        Assert.assertEquals(param2, matches.get(1).getText());
        Assert.assertEquals(param3, matches.get(2).getText());
    }

}
