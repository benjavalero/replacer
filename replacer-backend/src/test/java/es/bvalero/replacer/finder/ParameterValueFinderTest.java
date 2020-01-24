package es.bvalero.replacer.finder;

import es.bvalero.replacer.finder2.Immutable;
import es.bvalero.replacer.finder2.ImmutableFinder;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ParameterValueFinderTest {

    @Test
    public void testRegexParameterValue() {
        String value1 = "A\nvalue";
        String value2 = "Another value";
        String text = String.format("{{Template|index=%s\n| índice = %s }}", value1, value2);

        ImmutableFinder parameterValueFinder = new ParameterValueFinder();

        List<Immutable> matches = parameterValueFinder.findList(text);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(value1, matches.get(0).getText());
        Assert.assertEquals(value2, matches.get(1).getText());
    }
}
