package es.bvalero.replacer.finder;

import es.bvalero.replacer.finder2.Immutable;
import es.bvalero.replacer.finder2.ImmutableFinder;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TemplateNameFinderTest {

    @Test
    public void testRegexTemplateName() {
        String template1 = "Plantilla 1";
        String template2 = "Plantilla\n 2";
        String template3 = "Plantilla-3";

        String text = String.format("{{ %s\n| 1 }} {{%s}} {{%s:3}}", template1, template2, template3);

        ImmutableFinder templateNameFinder = new TemplateNameFinder();

        List<Immutable> matches = templateNameFinder.findList(text);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(template1, matches.get(0).getText());
        Assert.assertEquals(template2, matches.get(1).getText());
        Assert.assertEquals(template3, matches.get(2).getText());
    }
}
