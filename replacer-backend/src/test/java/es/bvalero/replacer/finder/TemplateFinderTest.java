package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TemplateFinderTest {

    @Test
    public void testRegexCompleteTemplate() {
        String template1 = "{{Cita|Un texto con {{Fecha|2019}} dentro.}}";
        String template2 = "{{cita|Otro\ntexto}}";
        String template3 = "{{ORDENAR:Apellido, Nombre}}";
        String template4 = "{{ cita\n| Spaces around }}";
        String text = String.format("%s %s %s %s", template1, template2, template3, template4);

        IgnoredReplacementFinder templateFinder = new TemplateFinder();

        List<MatchResult> matches = templateFinder.findIgnoredReplacements(text);
        Assert.assertEquals(4, matches.size());
        Assert.assertEquals(template1, matches.get(0).getText());
        Assert.assertEquals(template2, matches.get(1).getText());
        Assert.assertEquals(template3, matches.get(2).getText());
        Assert.assertEquals(template4, matches.get(3).getText());
    }

}
