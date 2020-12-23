package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TemplateNameFinderTest {

    private final TemplateNameFinder templateNameFinder = new TemplateNameFinder();

    @ParameterizedTest
    @CsvSource(
        value = {
            "{{ESP}}, ESP",
            "{{Cita|, Cita",
            "{{ Cita|, Cita",
            "'{{Cita\n|', Cita",
            "'{{\tCita|', Cita",
            "{{Cita web|, Cita web",
            "{{Cita_web|, Cita_web",
        }
    )
    void testFindTemplateNames(String text, String name) {
        List<Immutable> matches = templateNameFinder.findList(text);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(name, matches.get(0).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "{{!}}", "{{=}}", "{{#expr:xxx}}" })
    void testFindTemplateNamesNonValid(String text) {
        List<Immutable> matches = templateNameFinder.findList(text);

        Assertions.assertTrue(matches.isEmpty());
    }

    @Test
    void testSeveralTemplateNames() {
        String text = "En {{ESP}}, {{Cita|title=x}} y {{FRA}}.";

        TemplateNameFinder templateNameFinder = new TemplateNameFinder();
        List<Immutable> matches = templateNameFinder.findList(text);

        Assertions.assertEquals(3, matches.size());
    }
}
