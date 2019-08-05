package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TemplateFinderTest {

    private List<String> words;
    private String text;
    private Set<MatchResult> expected;

    @Before
    public void setUp() {
        String template1 = "{{Cita|Un texto con {{Fecha|2019}} dentro.}}";
        String template2 = "{{cita|Otro\ntexto}}";
        String template3 = "{{ORDENAR:Apellido, Nombre}}";
        String template4 = "{{ cita | Spaces around }}";
        this.text = String.format("%s %s %s %s", template1, template2, template3, template4);

        this.words = Arrays.asList("cita", "ORDENAR");

        this.expected = new HashSet<>();
        this.expected.add(new MatchResult(0, template1));
        this.expected.add(new MatchResult(45, template2));
        this.expected.add(new MatchResult(65, template3));
        this.expected.add(new MatchResult(94, template4));
    }

    @Test
    public void testTemplateRegexFinder() {
        TemplateRegexFinder finder = new TemplateRegexFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testTemplateRegexClassFinder() {
        TemplateRegexClassFinder finder = new TemplateRegexClassFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testTemplateRegexAllFinder() {
        TemplateRegexAllFinder finder = new TemplateRegexAllFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testTemplateRegexClassAllFinder() {
        TemplateRegexClassAllFinder finder = new TemplateRegexClassAllFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testTemplateAutomatonFinder() {
        TemplateAutomatonFinder finder = new TemplateAutomatonFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testTemplateAutomatonClassFinder() {
        TemplateAutomatonClassFinder finder = new TemplateAutomatonClassFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testTemplateAutomatonAllFinder() {
        TemplateAutomatonAllFinder finder = new TemplateAutomatonAllFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

    @Test
    public void testTemplateAutomatonClassAllFinder() {
        TemplateAutomatonClassAllFinder finder = new TemplateAutomatonClassAllFinder(words);
        Assert.assertEquals(expected, finder.findMatches(text));
    }

}
