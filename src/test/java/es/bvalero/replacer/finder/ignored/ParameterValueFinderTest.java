package es.bvalero.replacer.finder.ignored;

import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ParameterValueFinderTest {

    @Test
    public void testRegexParameterValue() {
        String value1 = "yyyy \\n zzz";
        String value2 = "xxx";
        String text = "{{Plantilla | índice = " + value1 + "|index= " + value2 + "}}";

        IgnoredReplacementFinder parameterValueFinder = new ParameterValueFinder();

        List<ArticleReplacement> matches = parameterValueFinder.findIgnoredReplacements(text);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(value1, matches.get(0).getText());
        Assert.assertEquals(value2, matches.get(1).getText());
    }

}
