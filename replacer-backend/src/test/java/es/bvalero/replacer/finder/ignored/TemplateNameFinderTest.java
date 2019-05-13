package es.bvalero.replacer.finder.ignored;

import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.finder.MatchResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TemplateNameFinderTest {

    @Test
    public void testRegexTemplateName() {
        String template1 = "Plantilla 1";
        String template2 = "Plantilla\n 2";
        String template3 = "Plantilla-3";

        String text = "xxx {{" + template1 + "| yyy }} / {{" + template2 + "}} / {{" + template3 + ":zzz}}.";

        IgnoredReplacementFinder templateNameFinder = new TemplateNameFinder();

        List<MatchResult> matches = templateNameFinder.findIgnoredReplacements(text);
        Assert.assertEquals(3, matches.size());
        Assert.assertEquals(template1, matches.get(0).getText());
        Assert.assertEquals(template2, matches.get(1).getText());
        Assert.assertEquals(template3, matches.get(2).getText());
    }

}
