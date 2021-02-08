package es.bvalero.replacer.finder.benchmark.template;

import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = XmlConfiguration.class)
class TemplateFinderTest {

    @Resource
    private List<String> templateNames;

    private String text;
    private Set<BenchmarkResult> expected;

    @BeforeEach
    public void setUp() {
        String template1 = "{{Cita|Un texto con {{Fecha|2019}} dentro.}}";
        String template2 = "{{cita|Otro\ntexto}}";
        String template3 = "{{ORDENAR:Apellido, Nombre}}";
        String template4 = "{{ cita libro\n| Spaces around }}";
        String template5 = "{{cite book | text}}";
        String template6 = "{{Traducido ref|Text}}";
        String template7 = "{{#expr: -1/3 round 0 }}";

        this.text =
            String.format(
                "En %s %s %s %s %s %s %s.",
                template1,
                template2,
                template3,
                template4,
                template5,
                template6,
                template7
            );

        this.expected = new HashSet<>();
        this.expected.add(BenchmarkResult.of(3, template1));
        this.expected.add(BenchmarkResult.of(48, template2));
        this.expected.add(BenchmarkResult.of(68, template3));
        this.expected.add(BenchmarkResult.of(97, template4));
        this.expected.add(BenchmarkResult.of(130, template5));
        this.expected.add(BenchmarkResult.of(151, template6));
        this.expected.add(BenchmarkResult.of(174, template7));
    }

    @Test
    void testTemplateRegexIteratedFinder() {
        TemplateRegexIteratedFinder finder = new TemplateRegexIteratedFinder(templateNames);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testTemplateRegexAlternateFinder() {
        TemplateRegexAlternateFinder finder = new TemplateRegexAlternateFinder(templateNames);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testTemplateAutomatonIteratedFinder() {
        TemplateAutomatonIteratedFinder finder = new TemplateAutomatonIteratedFinder(templateNames);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    void testTemplateAutomatonAlternateFinder() {
        TemplateAutomatonAlternateFinder finder = new TemplateAutomatonAlternateFinder(templateNames);
        Assertions.assertEquals(expected, finder.findMatches(text));
    }
}
