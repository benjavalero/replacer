package es.bvalero.replacer.finder.benchmark.ignorabletemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.util.Set;
import javax.annotation.Resource;
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
        assertFalse(finder.findMatches("xxx #REDIRECCIÓN [[A]] yyy").isEmpty());
        assertFalse(finder.findMatches("xxx #redirección [[A]] yyy").isEmpty());
        assertFalse(finder.findMatches("xxx #REDIRECT [[A]] yyy").isEmpty());
        assertTrue(finder.findMatches("Otro contenido").isEmpty());
        assertFalse(finder.findMatches("xxx {{destruir|motivo}}").isEmpty());
        // Test it is not ignored by containing "{{pa}}
        assertTrue(finder.findMatches("Text {{Partial|Co-Director}} Text").isEmpty());
    }
}
