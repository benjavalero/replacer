package es.bvalero.replacer.finder.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.WikipediaPage;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class TemplateUtilsTest {

    @Test
    void testFindTemplate() {
        String template = "{{Template|Text}}";

        WikipediaPage page = WikipediaPage.of(template);
        List<LinearMatchResult> matches = TemplateUtils.findAllTemplates(page);

        assertEquals(1, matches.size());
        assertEquals(template, matches.get(0).group());
    }

    @Test
    void testFindTemplateTruncated() {
        String template = "{{Template|Text";

        WikipediaPage page = WikipediaPage.of(template);
        List<LinearMatchResult> matches = TemplateUtils.findAllTemplates(page);

        assertTrue(matches.isEmpty());
    }

    @Test
    void testNestedTemplate() {
        String template2 = "{{Template2|Text2}}";
        String template = String.format("{{Template|Text %s Text}}", template2);

        WikipediaPage page = WikipediaPage.of(template);
        List<LinearMatchResult> matches = TemplateUtils.findAllTemplates(page);

        assertEquals(2, matches.size());
        assertEquals(template, matches.get(0).group());
        assertEquals(template2, matches.get(1).group());
    }

    @Test
    void testNestedTemplates() {
        String template3 = "{{Template3|Text3}}";
        String template2 = "{{Template2|Text2}}";
        String template = String.format("{{Template|Text %s Text %s Text}}", template2, template3);

        WikipediaPage page = WikipediaPage.of(template);
        List<LinearMatchResult> matches = TemplateUtils.findAllTemplates(page);

        Set<String> templates = Set.of(template, template2, template3);
        assertEquals(templates, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }

    @Test
    void testNestedTemplate2() {
        String template3 = "{{Template3|Text3}}";
        String template2 = String.format("{{Template2|Text2 %s Text2}}", template3);
        String template = String.format("{{Template|Text %s Text}}", template2);

        WikipediaPage page = WikipediaPage.of(template);
        List<LinearMatchResult> matches = TemplateUtils.findAllTemplates(page);

        assertEquals(3, matches.size());
        assertEquals(template, matches.get(0).group());
        assertEquals(template2, matches.get(1).group());
        assertEquals(template3, matches.get(2).group());
    }

    @Test
    void testNestedTemplate2Truncated() {
        String template3 = "{{Template3|Text3}}";
        String template2 = String.format("{{Template2|Text2 %s Text2}}", template3);
        String template = String.format("{{Template|Text %s Text", template2);

        WikipediaPage page = WikipediaPage.of(template);
        List<LinearMatchResult> matches = TemplateUtils.findAllTemplates(page);

        assertEquals(2, matches.size());
        assertEquals(template2, matches.get(0).group());
        assertEquals(template3, matches.get(1).group());
    }

    @Test
    void testFindTemplates() {
        String template1 = "{{Template1|Text1}}";
        String template2 = "{{Template2|Text2}}";
        String text = String.format("%s %s", template1, template2);

        WikipediaPage page = WikipediaPage.of(text);
        List<LinearMatchResult> matches = TemplateUtils.findAllTemplates(page);

        List<String> expected = List.of(template1, template2);
        assertEquals(expected, matches.stream().map(MatchResult::group).collect(Collectors.toList()));
    }

    @Test
    void testFindFakeTemplate() {
        String fake = "<math display=\"block\">Dw_{c,f,x}= {{\\sum(c*dw^2) \\over \\sum Dw}+10 \\over 2}</math>";
        String template1 = "{{Template1|Text1}}";
        String template2 = "{{Template2|Text2}}";
        String text = String.format("%s %s %s", fake, template1, template2);

        WikipediaPage page = WikipediaPage.of(text);
        List<LinearMatchResult> matches = TemplateUtils.findAllTemplates(page);

        Set<String> expected = Set.of(template1, template2);
        assertEquals(expected, matches.stream().map(MatchResult::group).collect(Collectors.toSet()));
    }
}
