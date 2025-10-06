package es.bvalero.replacer.finder.immutable.finders;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.listing.find.ListingOfflineFinder;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.listing.parse.ComposedMisspellingParser;
import es.bvalero.replacer.finder.listing.parse.SimpleMisspellingParser;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("offline")
@EnableConfigurationProperties(FinderProperties.class)
@SpringBootTest(
    classes = {
        TemplateFinder.class,
        UppercaseFinder.class,
        SimpleMisspellingLoader.class,
        ComposedMisspellingLoader.class,
        ListingOfflineFinder.class,
        SimpleMisspellingParser.class,
        ComposedMisspellingParser.class,
    }
)
class TemplateFinderTest {

    @Autowired
    private SimpleMisspellingLoader simpleMisspellingLoader;

    @Autowired
    private ComposedMisspellingLoader composedMisspellingLoader;

    @Autowired
    private TemplateFinder templateFinder;

    @ParameterizedTest
    @CsvSource(
        value = { "{{Template1}}, Template1", "{{Template2:Text2}}, Template2", "{{Template3|param=value}}, Template3" }
    )
    void testTemplateNames(String text, String templateName) {
        List<Immutable> matches = templateFinder.findList(text);

        assertTrue(matches.stream().map(Immutable::text).collect(Collectors.toSet()).contains(templateName));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "{{Cita libro|param=value}}",
            "{{#expr:Text2}}",
            "{{IMDb nombre|1229738|Luis Posada}}",
            "{{Enlace_roto|2=http://www.example.com}}",
            "{{lang-ho|Papua Niu Gini}}",
        }
    )
    void testTemplateComplete(String text) {
        List<Immutable> matches = templateFinder.findList(text);

        assertEquals(1, matches.size());
        assertEquals(text, matches.get(0).text());
    }

    @ParameterizedTest
    @ValueSource(strings = { "{{Template3|param=value}}" })
    void testTemplateNotComplete(String text) {
        List<Immutable> matches = templateFinder.findList(text);

        assertFalse(matches.isEmpty());
        assertFalse(matches.stream().map(Immutable::text).collect(Collectors.toSet()).contains(text));
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "{{Template1|param1= valor1}}, param1, ", // Only param
            "{{Template3|url= valor3 }}, url, valor3", // Param + value
            "{{Template4|param4= xxx.jpg }}, param4, xxx.jpg", // Param + value
            "{{Fs player|nat=Brazil}}, nat, Brazil", // Param + value
        }
    )
    void testParamValues(String text, String param, String value) {
        List<Immutable> matches = templateFinder.findList(text);

        assertFalse(matches.isEmpty());
        assertTrue(matches.stream().map(Immutable::text).map(String::trim).collect(Collectors.toSet()).contains(param));
        if (StringUtils.isBlank(value)) {
            assertEquals(2, matches.size()); // Template name + param
        } else {
            assertEquals(3, matches.size()); // Template name + param + value
            assertTrue(
                matches.stream().map(Immutable::text).map(String::trim).collect(Collectors.toSet()).contains(value)
            );
        }
    }

    @Test
    void testCiteValue() {
        String text = "{{P|ps= «Libro Nº 34, año 1825, f. 145).»}}";

        List<Immutable> matches = templateFinder.findList(text);

        Set<String> expected = Set.of("P", "ps");
        Set<String> actual = matches.stream().map(Immutable::text).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void testValueWithComment() {
        String text = "{{Template|image = x.jpg <!-- A comment -->}}";

        List<Immutable> matches = templateFinder.findList(text);

        Set<String> expected = Set.of("Template", "image ", " x.jpg ");
        Set<String> actual = matches.stream().map(Immutable::text).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void testValueWithSpecialTemplate() {
        String text = "{{Template|image = x.jpg{{!}}More text}}";

        List<Immutable> matches = templateFinder.findList(text);

        Set<String> expected = Set.of("Template", "image ", " x.jpg");
        Set<String> actual = matches.stream().map(Immutable::text).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void testNestedTemplateValues() {
        String template2 = "{{Template2|url=value2}}";
        String template = String.format("{{Template1|param1=%s}}", template2);

        List<Immutable> matches = templateFinder.findList(template);

        Set<String> expected = Set.of("Template1", "Template2", "param1", "url", "value2");
        Set<String> actual = matches.stream().map(Immutable::text).collect(Collectors.toSet());
        assertEquals(expected, actual);

        // Check positions
        assertEquals(
            2,
            matches.stream().filter(m -> m.text().equals("Template1")).findAny().map(Immutable::start).orElse(0)
        );
        assertEquals(
            12,
            matches.stream().filter(m -> m.text().equals("param1")).findAny().map(Immutable::start).orElse(0)
        );
        assertEquals(
            21,
            matches.stream().filter(m -> m.text().equals("Template2")).findAny().map(Immutable::start).orElse(0)
        );
        assertEquals(
            31,
            matches.stream().filter(m -> m.text().equals("url")).findAny().map(Immutable::start).orElse(0)
        );
        assertEquals(
            35,
            matches.stream().filter(m -> m.text().equals("value2")).findAny().map(Immutable::start).orElse(0)
        );
    }

    @Test
    void testSpecialCharacters() {
        String text = "{{!!!}}";

        List<Immutable> matches = templateFinder.findList(text);

        assertTrue(matches.isEmpty());
    }

    @Test
    void testContiguousTemplates() {
        String text = "{{T|x={{A}}{{B}} |y=C}}";

        List<Immutable> matches = templateFinder.findList(text);

        Set<String> expected = Set.of("T", "x", "A", "B", "y");
        Set<String> actual = matches.stream().map(Immutable::text).collect(Collectors.toSet());
        assertEquals(expected, actual);
        assertTrue(matches.stream().allMatch(m -> m.start() >= 0));
    }

    @Test
    void testRepeatedParameters() {
        String text = "{{T|x = A|x = A}}";

        List<Immutable> matches = templateFinder.findList(text);

        Set<Immutable> expected = Set.of(Immutable.of(2, "T"), Immutable.of(4, "x "), Immutable.of(10, "x "));
        Set<Immutable> actual = new HashSet<>(matches);
        assertEquals(3, matches.size());
        assertEquals(expected, actual);
    }

    @Test
    void testSimilarParameters() {
        String text = "{{T|image_caption=A|image=A}}";

        List<Immutable> matches = templateFinder.findList(text);

        Set<Immutable> expected = Set.of(
            Immutable.of(2, "T"),
            Immutable.of(4, "image_caption"),
            Immutable.of(20, "image")
        );
        Set<Immutable> actual = new HashSet<>(matches);
        assertEquals(3, matches.size());
        assertEquals(expected, actual);
    }

    @Test
    void testValueWithLink() {
        String text = "{{T|x=[[A|B]]|y=C}}";

        List<Immutable> matches = templateFinder.findList(text);

        // To calculate the parameter position we assume the parameters are not repeated in the template
        // Therefore in this case though we find both parameters always the first position is returned
        Set<Immutable> expected = Set.of(Immutable.of(2, "T"), Immutable.of(4, "x"), Immutable.of(14, "y"));
        Set<Immutable> actual = new HashSet<>(matches);
        assertEquals(expected, actual);
    }

    @Test
    void testArgumentWithFile() {
        // The second parameter may appear a file if we don't check the extension
        String text = "{{T|xxx.jpg|22.42}}";

        List<Immutable> matches = templateFinder.findList(text);

        Set<Immutable> expected = Set.of(Immutable.of(2, "T"), Immutable.of(4, "xxx.jpg"));
        Set<Immutable> actual = new HashSet<>(matches);
        assertEquals(expected, actual);
    }

    @Test
    void testArgumentWithUppercase() {
        // Load misspellings
        simpleMisspellingLoader.load();

        String text = "{{T|Enero de 1980|p= Febrero de 1979}}";

        List<Immutable> matches = templateFinder.findList(text);

        Set<Immutable> expected = Set.of(
            Immutable.of(2, "T"),
            Immutable.of(4, "Enero"),
            Immutable.of(18, "p"),
            Immutable.of(21, "Febrero")
        );
        Set<Immutable> actual = new HashSet<>(matches);
        assertEquals(expected, actual);
    }

    @Test
    void testArgumentWithUppercaseComposed() {
        // Load misspellings
        composedMisspellingLoader.load();

        String text = "{{T|Guerras napoleónicas}}";

        List<Immutable> matches = templateFinder.findList(text);

        Set<Immutable> expected = Set.of(Immutable.of(2, "T"), Immutable.of(4, "Guerras napoleónicas"));
        Set<Immutable> actual = new HashSet<>(matches);
        assertEquals(expected, actual);
    }

    @Test
    void testArgumentWithReference() {
        // Load misspellings
        simpleMisspellingLoader.load();

        String text = "{{T|argument<ref name=\"X\" />}}";

        List<Immutable> matches = templateFinder.findList(text);

        // The '=' should be detected as belonging to the reference,
        // and therefore the argument must be treated as a value
        Set<String> expected = Set.of("T");
        Set<String> actual = matches.stream().map(Immutable::text).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void testIgnorableTemplate() {
        assertTrue(templateFinder.findList("Otro contenido").isEmpty());
        assertFalse(templateFinder.findList("xxx {{destruir|motivo}}").isEmpty());
    }

    @Test
    void testFakeTemplate() {
        String fake = "<math display=\"block\">Dw_{c,f,x}= {{\\sum(c*dw^2) \\over \\sum Dw}+10 \\over 2}</math>";
        String template1 = "{{Template1|Text1}}";
        String template2 = "{{Template2|Text2}}";
        String text = String.format("%s %s %s", template1, template2, fake);

        List<Immutable> matches = templateFinder.findList(text);

        Set<String> expected = Set.of("Template1", "Template2");
        Set<String> actual = matches.stream().map(Immutable::text).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void testNestedTemplatesFirstWithNoValue() {
        // In theory, it is a bad constructed template, so we should not check this.
        String template2 = "{{Template2|url=value2}}";
        String template = String.format("{{Template1|%s}}", template2);

        List<Immutable> matches = templateFinder.findList(template);

        Set<String> expected = Set.of("Template1", "Template2", "url", "value2");
        Set<String> actual = matches.stream().map(Immutable::text).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }

    @Test
    void testNestedTemplateBeforeParameter() {
        String template = "{{#ifeq:{{NAMESPACE}}|Anexo}}";

        List<Immutable> matches = templateFinder.findList(template);

        Set<String> expected = Set.of("#ifeq", "NAMESPACE");
        Set<String> actual = matches.stream().map(Immutable::text).collect(Collectors.toSet());
        assertEquals(expected, actual);
    }
}
