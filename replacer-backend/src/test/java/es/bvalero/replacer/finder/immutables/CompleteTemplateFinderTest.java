package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.LinearMatcher;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { CompleteTemplateFinder.class, XmlConfiguration.class })
class CompleteTemplateFinderTest {

    @Autowired
    private CompleteTemplateFinder completeTemplateFinder;

    @Test
    void testFindTemplate() {
        String template = "{{Template|Text}}";

        WikipediaPage page = WikipediaPage.builder().content(template).build();
        List<LinearMatcher> matches = completeTemplateFinder.findAllTemplates(page);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(template, matches.get(0).group());
    }

    @Test
    void testFindTemplateTruncated() {
        String template = "{{Template|Text";

        WikipediaPage page = WikipediaPage.builder().content(template).build();
        List<LinearMatcher> matches = completeTemplateFinder.findAllTemplates(page);

        Assertions.assertTrue(matches.isEmpty());
    }

    @Test
    void testNestedTemplate() {
        String template2 = "{{Template2|Text2}}";
        String template = String.format("{{Template|Text %s Text}}", template2);

        WikipediaPage page = WikipediaPage.builder().content(template).build();
        List<LinearMatcher> matches = completeTemplateFinder.findAllTemplates(page);

        Assertions.assertEquals(2, matches.size());
        Assertions.assertEquals(template, matches.get(0).group());
        Assertions.assertEquals(template2, matches.get(1).group());
    }

    @Test
    void testNestedTemplates() {
        String template3 = "{{Template3|Text3}}";
        String template2 = "{{Template2|Text2}}";
        String template = String.format("{{Template|Text %s Text %s Text}}", template2, template3);

        WikipediaPage page = WikipediaPage.builder().content(template).build();
        List<LinearMatcher> matches = completeTemplateFinder.findAllTemplates(page);

        Set<String> templates = Set.of(template, template2, template3);
        Assertions.assertEquals(templates, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testNestedTemplate2() {
        String template3 = "{{Template3|Text3}}";
        String template2 = String.format("{{Template2|Text2 %s Text2}}", template3);
        String template = String.format("{{Template|Text %s Text}}", template2);

        WikipediaPage page = WikipediaPage.builder().content(template).build();
        List<LinearMatcher> matches = completeTemplateFinder.findAllTemplates(page);

        Assertions.assertEquals(3, matches.size());
        Assertions.assertEquals(template, matches.get(0).group());
        Assertions.assertEquals(template2, matches.get(1).group());
        Assertions.assertEquals(template3, matches.get(2).group());
    }

    @Test
    void testNestedTemplate2Truncated() {
        String template3 = "{{Template3|Text3}}";
        String template2 = String.format("{{Template2|Text2 %s Text2}}", template3);
        String template = String.format("{{Template|Text %s Text", template2);

        WikipediaPage page = WikipediaPage.builder().content(template).build();
        List<LinearMatcher> matches = completeTemplateFinder.findAllTemplates(page);

        Assertions.assertEquals(2, matches.size());
        Assertions.assertEquals(template2, matches.get(0).group());
        Assertions.assertEquals(template3, matches.get(1).group());
    }

    @Test
    void testFindTemplates() {
        String template1 = "{{Template1|Text1}}";
        String template2 = "{{Template2|Text2}}";
        String text = String.format("%s %s", template1, template2);

        WikipediaPage page = WikipediaPage.builder().content(text).build();
        List<LinearMatcher> matches = completeTemplateFinder.findAllTemplates(page);

        List<String> expected = List.of(template1, template2);
        Assertions.assertEquals(expected, matches.stream().map(MatchResult::group).collect(Collectors.toList()));
    }

    @Test
    void testFindTemplateNames() {
        String template1 = "{{Template1}}";
        String template2 = "{{Template2:Text2}}";
        String template3 = "{{Template3|param=value}}";
        String text = String.format("%s %s %s", template1, template2, template3);

        List<Immutable> matches = completeTemplateFinder.findList(text);

        List<String> names = List.of("Template1", "Template2", "Template3");
        Assertions.assertTrue(matches.stream().map(Immutable::getText).collect(Collectors.toSet()).containsAll(names));
    }

    @Test
    void testFindCompleteTemplates() {
        String template1 = "{{Cita libro|param=value}}";
        String template2 = "{{#tag:Text2}}";
        String template3 = "{{Template3|param=value}}"; // Not captured
        String text = String.format("%s %s %s", template1, template2, template3);

        List<Immutable> matches = completeTemplateFinder.findList(text);

        List<String> templates = List.of(template1, template2);
        Assertions.assertTrue(
            matches.stream().map(Immutable::getText).collect(Collectors.toSet()).containsAll(templates)
        );
        Assertions.assertFalse(
            matches.stream().map(Immutable::getText).collect(Collectors.toSet()).contains(template3)
        );
    }

    @Test
    void testFindParamValues() {
        String template1 = "{{Template1|param1=valor1}}";
        String template2 = "{{Template2|índice=valor2}}";
        String template3 = "{{Template3|param3=album}}";
        String template4 = "{{Template4|param4=xxx.jpg}}";
        String text = String.format("%s %s %s %s", template1, template2, template3, template4);

        List<Immutable> matches = completeTemplateFinder.findList(text);

        List<String> params = List.of("param1", "índice", "param3", "param4");
        List<String> values = List.of("valor2", "album", "xxx.jpg");
        Assertions.assertTrue(matches.stream().map(Immutable::getText).collect(Collectors.toSet()).containsAll(params));
        Assertions.assertTrue(matches.stream().map(Immutable::getText).collect(Collectors.toSet()).containsAll(values));
        Assertions.assertFalse(matches.stream().map(Immutable::getText).collect(Collectors.toSet()).contains("valor1"));
    }

    @Test
    void testCiteValue() {
        String text = "{{P|ps= «Libro Nº 34, año 1825, f. 145).»}}";

        List<Immutable> matches = completeTemplateFinder.findList(text);

        Set<String> expected = Set.of("P", "ps");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testValueWithComment() {
        String text = "{{Template|image = x.jpg <!-- A comment -->}}";

        List<Immutable> matches = completeTemplateFinder.findList(text);

        Set<String> expected = Set.of("Template", "image ", "x.jpg");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testNestedTemplateValues() {
        String template2 = "{{Template2|índice=value2}}";
        String template = String.format("{{Template1|param1=%s}}", template2);

        List<Immutable> matches = completeTemplateFinder.findList(template);

        Set<String> expected = Set.of("Template1", "Template2", "param1", "índice", "value2");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);

        // Check positions
        Assertions.assertEquals(
            2,
            matches.stream().filter(m -> m.getText().equals("Template1")).findAny().get().getStart()
        );
        Assertions.assertEquals(
            12,
            matches.stream().filter(m -> m.getText().equals("param1")).findAny().get().getStart()
        );
        Assertions.assertEquals(
            21,
            matches.stream().filter(m -> m.getText().equals("Template2")).findAny().get().getStart()
        );
        Assertions.assertEquals(
            31,
            matches.stream().filter(m -> m.getText().equals("índice")).findAny().get().getStart()
        );
        Assertions.assertEquals(
            38,
            matches.stream().filter(m -> m.getText().equals("value2")).findAny().get().getStart()
        );
    }

    @Test
    void testSpecialCharacter() {
        String text = "{{|}}";

        List<Immutable> matches = completeTemplateFinder.findList(text);

        Assertions.assertTrue(matches.isEmpty());
    }

    @Test
    void testContiguousTemplates() {
        String text = "{{T|x={{A}}{{B}} |y}}";

        List<Immutable> matches = completeTemplateFinder.findList(text);

        Set<String> expected = Set.of("T", "x", "A", "B", "y");
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
        Assertions.assertTrue(matches.stream().allMatch(m -> m.getStart() >= 0));
    }
}
