package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.composed.AcuteOFinder;
import es.bvalero.replacer.finder.misspelling.MisspellingComposedFinder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { TemplateFinder.class, XmlConfiguration.class })
class TemplateFinderTest {
    @Autowired
    private TemplateFinder templateFinder;

    @ParameterizedTest
    @ValueSource(
        strings = {
            "{{Cita|Un texto con {{Fecha|2019}} dentro.}}",
            "{{cita|Otro\ntexto}}",
            "{{ORDENAR:Apellido, Nombre}}",
            "{{ cita libro\n| Spaces around }}",
            "{{cite book\t| text}}",
            "{{Traducido ref|Text}}",
            "{{#expr: -1/3 round 0 }}",
            "{{lang|en|A text in English}}",
            "{{lang-en | Another text in English}}",
        }
    )
    void testRegexCompleteTemplate(String template) {
        String text = String.format("En %s.", template);
        List<Immutable> matches = templateFinder.findList(text);
        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(template, matches.get(0).getText());
    }
}
