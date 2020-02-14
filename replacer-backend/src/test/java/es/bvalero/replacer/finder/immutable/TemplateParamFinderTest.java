package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TemplateParamFinderTest {

    @Test
    public void testRegexTemplateParam() {
        String param1 = "param 1";
        String param2 = "parám_2";
        String param3 = "param-3";
        String param4 = "param4";
        String link = "[[A|B]]\n==Section==";
        String text = String.format(
            "{{Template| %s = value1 |\t%s\t= value2 |%s|%s=}} %s",
            param1,
            param2,
            param3,
            param4,
            link
        );

        ImmutableFinder templateParamFinder = new TemplateParamFinder();

        List<Immutable> matches = templateParamFinder.findList(text);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(param1, matches.get(0).getText());
        Assert.assertEquals(param2, matches.get(1).getText());
        Assert.assertEquals(param4, matches.get(2).getText());
    }
}
