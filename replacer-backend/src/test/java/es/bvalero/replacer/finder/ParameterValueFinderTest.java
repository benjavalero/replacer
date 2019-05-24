package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;

public class ParameterValueFinderTest {

    @Test
    public void testRegexParameterValue() {
        String value1 = "A\nvalue\n";
        String value2 = " Another value ";
        String text = String.format("{{Template|index=%s| índice =%s}}", value1, value2);

        IgnoredReplacementFinder parameterValueFinder = new ParameterValueFinder();

        List<MatchResult> matches = parameterValueFinder.findIgnoredReplacements(text);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(value1, matches.get(0).getText());
        Assert.assertEquals(value2, matches.get(1).getText());
    }

}
