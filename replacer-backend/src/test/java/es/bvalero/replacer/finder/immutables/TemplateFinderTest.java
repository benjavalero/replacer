package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { TemplateFinder.class, XmlConfiguration.class })
public class TemplateFinderTest {
    @Autowired
    private TemplateFinder templateFinder;

    @Test
    public void testRegexCompleteTemplate() {
        String template1 = "{{Cita|Un texto con {{Fecha|2019}} dentro.}}";
        String template2 = "{{cita|Otro\ntexto}}";
        String template3 = "{{ORDENAR:Apellido, Nombre}}";
        String template4 = "{{ cita libro\n| Spaces around }}";
        String template5 = "{{cite book | text}}";
        String template6 = "{{Traducido ref|Text}}";
        String template7 = "{{#expr: -1/3 round 0 }}";
        String text = String.format(
            "En %s %s %s %s %s %s %s.",
            template1,
            template2,
            template3,
            template4,
            template5,
            template6,
            template7
        );

        List<Immutable> matches = templateFinder.findList(text);

        List<String> expected = Arrays.asList(
            template1,
            template2,
            template3,
            template4,
            template5,
            template6,
            template7
        );
        Assertions.assertEquals(expected, matches.stream().map(Immutable::getText).collect(Collectors.toList()));
    }
}
