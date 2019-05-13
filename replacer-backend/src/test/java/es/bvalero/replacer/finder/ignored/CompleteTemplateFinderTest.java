package es.bvalero.replacer.finder.ignored;

import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.finder.MatchResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CompleteTemplateFinderTest {

    @Test
    public void testRegexCompleteTemplate() {
        String template1 = "{{Cita|xxx {{Fecha|zzz}} yyy}}";
        String template2 = "{{quote|bbb}}";
        String text = "xxx " + template1 + " / " + template2 + " zzz";

        IgnoredReplacementFinder finder = new CompleteTemplateFinder();

        List<MatchResult> matches = finder.findIgnoredReplacements(text);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(template1, matches.get(0).getText());
        Assert.assertEquals(template2, matches.get(1).getText());
    }

    @Test
    public void testRegexCategory() {
        String category1 = "[[Categoría:Lluvia]]";
        String category2 = "[[Categoría:España cañí]]";
        String text = "xxx " + category1 + " / " + category2 + " zzz";

        IgnoredReplacementFinder finder = new CompleteTemplateFinder();

        List<MatchResult> matches = finder.findIgnoredReplacements(text);
        Assert.assertEquals(2, matches.size());
        Assert.assertEquals(category1, matches.get(0).getText());
        Assert.assertEquals(category2, matches.get(1).getText());
    }

}
