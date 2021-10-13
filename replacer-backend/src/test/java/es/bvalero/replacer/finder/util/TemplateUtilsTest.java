package es.bvalero.replacer.finder.util;

import es.bvalero.replacer.finder.FinderPage;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TemplateUtilsTest {

    @Test
    void testFindTemplate() {
        String template = "{{Template|Text}}";

        FinderPage page = FinderPage.of(template);
        List<LinearMatchResult> matches = TemplateUtils.findAllTemplates(page);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(template, matches.get(0).group());
    }

    @Test
    void testFindTemplateTruncated() {
        String template = "{{Template|Text";

        FinderPage page = FinderPage.of(template);
        List<LinearMatchResult> matches = TemplateUtils.findAllTemplates(page);

        Assertions.assertTrue(matches.isEmpty());
    }

    @Test
    void testNestedTemplate() {
        String template2 = "{{Template2|Text2}}";
        String template = String.format("{{Template|Text %s Text}}", template2);

        FinderPage page = FinderPage.of(template);
        List<LinearMatchResult> matches = TemplateUtils.findAllTemplates(page);

        Assertions.assertEquals(2, matches.size());
        Assertions.assertEquals(template, matches.get(0).group());
        Assertions.assertEquals(template2, matches.get(1).group());
    }

    @Test
    void testNestedTemplates() {
        String template3 = "{{Template3|Text3}}";
        String template2 = "{{Template2|Text2}}";
        String template = String.format("{{Template|Text %s Text %s Text}}", template2, template3);

        FinderPage page = FinderPage.of(template);
        List<LinearMatchResult> matches = TemplateUtils.findAllTemplates(page);

        Set<String> templates = Set.of(template, template2, template3);
        Assertions.assertEquals(templates, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testNestedTemplate2() {
        String template3 = "{{Template3|Text3}}";
        String template2 = String.format("{{Template2|Text2 %s Text2}}", template3);
        String template = String.format("{{Template|Text %s Text}}", template2);

        FinderPage page = FinderPage.of(template);
        List<LinearMatchResult> matches = TemplateUtils.findAllTemplates(page);

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

        FinderPage page = FinderPage.of(template);
        List<LinearMatchResult> matches = TemplateUtils.findAllTemplates(page);

        Assertions.assertEquals(2, matches.size());
        Assertions.assertEquals(template2, matches.get(0).group());
        Assertions.assertEquals(template3, matches.get(1).group());
    }

    @Test
    void testFindTemplates() {
        String template1 = "{{Template1|Text1}}";
        String template2 = "{{Template2|Text2}}";
        String text = String.format("%s %s", template1, template2);

        FinderPage page = FinderPage.of(text);
        List<LinearMatchResult> matches = TemplateUtils.findAllTemplates(page);

        List<String> expected = List.of(template1, template2);
        Assertions.assertEquals(expected, matches.stream().map(MatchResult::group).collect(Collectors.toList()));
    }
}
