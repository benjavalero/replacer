package es.bvalero.replacer.finder.benchmark.ignorabletemplate;

import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.util.Set;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { XmlConfiguration.class })
class IgnorableTemplateFinderTest {

    @Resource
    private Set<String> ignorableTemplates;

    @Test
    void testIgnorableTemplateLowercaseContainsFinder() {
        IgnorableTemplateLowercaseContainsFinder finder = new IgnorableTemplateLowercaseContainsFinder(
            ignorableTemplates
        );
        doAssertions(finder);
    }

    @Test
    void testIgnorableTemplateRegexFinder() {
        IgnorableTemplateRegexFinder finder = new IgnorableTemplateRegexFinder(ignorableTemplates);
        doAssertions(finder);
    }

    @Test
    void testIgnorableTemplateRegexInsensitiveFinder() {
        IgnorableTemplateRegexInsensitiveFinder finder = new IgnorableTemplateRegexInsensitiveFinder(
            ignorableTemplates
        );
        doAssertions(finder);
    }

    @Test
    void testIgnorableTemplateAutomatonFinder() {
        IgnorableTemplateAutomatonFinder finder = new IgnorableTemplateAutomatonFinder(ignorableTemplates);
        doAssertions(finder);
    }

    private void doAssertions(BenchmarkFinder finder) {
        Assertions.assertFalse(finder.findMatches("xxx #REDIRECCIÓN [[A]] yyy").isEmpty());
        Assertions.assertFalse(finder.findMatches("xxx #redirección [[A]] yyy").isEmpty());
        Assertions.assertFalse(finder.findMatches("xxx #REDIRECT [[A]] yyy").isEmpty());
        Assertions.assertTrue(finder.findMatches("Otro contenido").isEmpty());
        Assertions.assertFalse(finder.findMatches("xxx {{destruir|motivo}}").isEmpty());
        // Test it is not ignored by containing "{{pa}}
        Assertions.assertTrue(finder.findMatches("Text {{Partial|Co-Director}} Text").isEmpty());
    }
}
