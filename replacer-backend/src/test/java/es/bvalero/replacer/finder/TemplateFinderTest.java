package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateFinderTest {

    @Test
    public void testRegexCompleteTemplate() {
        String template1 = "{{Cita|Un texto con {{Fecha|2019}} dentro.}}";
        String template2 = "{{cita|Otro\ntexto}}";
        String template3 = "{{ORDENAR:Apellido, Nombre}}";
        String template4 = "{{ cita libro\n| Spaces around }}";
        String template5 = "{{cite book | text}}";
        String template6 = "{{Traducido ref|Text}}";
        String text = String.format("En %s %s %s %s %s %s.", template1, template2, template3, template4, template5, template6);

        IgnoredReplacementFinder templateFinder = new TemplateFinder();
        List<MatchResult> matches = templateFinder.findIgnoredReplacements(text);

        List<String> expected = Arrays.asList(template1, template2, template3, template4, template5, template6);
        Assert.assertEquals(expected, matches.stream().map(MatchResult::getText).collect(Collectors.toList()));
    }

}
