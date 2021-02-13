package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.config.XmlConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { IgnorableTemplateFinder.class, XmlConfiguration.class })
class IgnorableTemplateFinderTest {

    @Autowired
    private IgnorableTemplateFinder ignorableTemplateFinder;

    @Test
    void testFindIgnorableTemplate() {
        Assertions.assertFalse(ignorableTemplateFinder.findList("xxx #REDIRECCIÓN [[A]] yyy").isEmpty());
        Assertions.assertFalse(ignorableTemplateFinder.findList("xxx #redirección [[A]] yyy").isEmpty());
        Assertions.assertFalse(ignorableTemplateFinder.findList("xxx #REDIRECT [[A]] yyy").isEmpty());
        Assertions.assertTrue(ignorableTemplateFinder.findList("Otro contenido").isEmpty());
        Assertions.assertFalse(ignorableTemplateFinder.findList("xxx {{destruir|motivo}}").isEmpty());
        // Test it is not ignored by containing "{{pa}}
        Assertions.assertTrue(ignorableTemplateFinder.findList("Text {{Partial|Co-Director}} Text").isEmpty());
    }
}
